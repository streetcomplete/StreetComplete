package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.ElementType.NODE
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.data.quest.NoCountriesExcept
import de.westnordost.streetcomplete.data.quest.OsmQuestKey
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.argThat
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OsmQuestControllerTest {

    private lateinit var db: OsmQuestDao
    private lateinit var hiddenDB: OsmQuestsHiddenDao
    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var notesSource: NotesWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var countryBoundaries: CountryBoundaries

    private lateinit var ctrl: OsmQuestController
    private lateinit var listener: OsmQuestSource.Listener
    private lateinit var hideListener: OsmQuestsHiddenSource.Listener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener
    private lateinit var notesListener: NotesWithEditsSource.Listener

    @BeforeTest fun setUp() {
        db = mock()

        hiddenDB = mock()
        mapDataSource = mock()

        notesSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to ApplicableQuestType,
            1 to NotApplicableQuestType,
            2 to ComplexQuestTypeApplicableToNode42,
            3 to ApplicableQuestTypeNotInAnyCountry,
            4 to ApplicableQuestType2
        ))
        countryBoundaries = mock()

        on(mapDataSource.addListener(any())).then { invocation ->
            mapDataListener = invocation.getArgument(0)
            Unit
        }

        on(notesSource.addListener(any())).then { invocation ->
            notesListener = invocation.getArgument(0)
            Unit
        }

        listener = mock()
        hideListener = mock()
        ctrl = OsmQuestController(db, hiddenDB, mapDataSource, notesSource, questTypeRegistry, lazyOf(countryBoundaries))
        ctrl.addListener(listener)
        ctrl.addListener(hideListener)
    }

    @Test fun get() {
        val key = osmQuestKey(NODE, 1, "ApplicableQuestType")
        val entry = questEntry(NODE, 1, "ApplicableQuestType")
        val g = pGeom()

        on(db.get(osmQuestKey(NODE, 1, "ApplicableQuestType"))).thenReturn(entry)
        on(mapDataSource.getGeometry(NODE, 1)).thenReturn(g)

        val expectedQuest = OsmQuest(ApplicableQuestType, NODE, 1, g)
        assertEquals(expectedQuest, ctrl.getVisible(key))
    }

    @Test fun getAllVisibleInBBox() {
        val notePos = p(0.5, 0.5)
        val entries = listOf(
            // ok!
            questEntry(elementType = NODE, elementId = 1),
            // hidden!
            questEntry(elementType = NODE, elementId = 2),
            // blacklisted position!
            questEntry(elementType = NODE, elementId = 3, position = notePos),
            // geometry not found!
            questEntry(elementType = NODE, elementId = 4),
        )
        val geoms = listOf(ElementPointGeometry(p()))
        val hiddenQuests = listOf(OsmQuestKey(NODE, 2, "ApplicableQuestType"))
        val bbox = bbox()

        on(hiddenDB.getAllIds()).thenReturn(hiddenQuests)
        on(notesSource.getAllPositions(any())).thenReturn(listOf(notePos))
        on(db.getAllInBBox(bbox, null)).thenReturn(entries)
        on(mapDataSource.getGeometries(argThat {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(NODE, 1),
                ElementKey(NODE, 4),
            ))
        })).thenReturn(listOf(
            ElementGeometryEntry(NODE, 1, geoms[0])
        ))

        val expectedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geoms[0]),
        )
        assertTrue(ctrl.getAllVisibleInBBox(bbox, null).containsExactlyInAnyOrder(expectedQuests))
    }

    @Test fun getAllHiddenNewerThan() {
        val geoms = listOf(
            ElementPointGeometry(p()),
            ElementPointGeometry(p()),
            ElementPointGeometry(p()),
        )

        on(hiddenDB.getNewerThan(123L)).thenReturn(listOf(
            // ok!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 1L, "ApplicableQuestType"), 250),
            // unknown quest type
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 2L, "UnknownQuestType"), 250),
            // no geometry!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 3L, "ApplicableQuestType"), 250),
        ))
        on(mapDataSource.getGeometries(argThat {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(NODE, 1),
                ElementKey(NODE, 2),
                ElementKey(NODE, 3)
            ))
        })).thenReturn(listOf(
            ElementGeometryEntry(NODE, 1, geoms[0]),
            ElementGeometryEntry(NODE, 2, geoms[1])
        ))

        assertEquals(
            listOf(
                OsmQuestHidden(NODE, 1, ApplicableQuestType, p(), 250)
            ),
            ctrl.getAllHiddenNewerThan(123L)
        )
    }

    @Test fun countAll() {
        on(hiddenDB.countAll()).thenReturn(123L)
        assertEquals(123L, ctrl.countAll())
    }

    @Test fun hide() {
        val quest = osmQuest(questType = ApplicableQuestType)

        on(hiddenDB.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())

        ctrl.hide(quest.key)

        verify(hiddenDB).add(quest.key)
        verify(hideListener).onHid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.type, quest.position, 555
        )))
        verify(listener).onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestKeys = eq(listOf(quest.key))
        )
    }

    @Test fun unhide() {
        val quest = osmQuest(questType = ApplicableQuestType)

        on(hiddenDB.delete(quest.key)).thenReturn(true)
        on(hiddenDB.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())
        on(db.get(quest.key)).thenReturn(quest)

        assertTrue(ctrl.unhide(quest.key))

        verify(hiddenDB).delete(quest.key)
        verify(hideListener).onUnhid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.type, quest.position, 555
        )))
        verify(listener).onUpdated(
            addedQuests = eq(listOf(quest)),
            deletedQuestKeys = eq(emptyList())
        )
    }

    @Test fun unhideAll() {
        on(hiddenDB.deleteAll()).thenReturn(2)
        assertEquals(2, ctrl.unhideAll())
        verify(listener).onInvalidated()
        verify(hideListener).onUnhidAll()
    }

    @Test fun `updates quests on notes listener update`() {
        val notes = listOf(note(1))

        notesListener.onUpdated(added = notes, updated = emptyList(), deleted = emptyList())

        verify(listener).onInvalidated()
    }

    @Test fun `updates quests on map data listener update for deleted elements`() {
        val quests = listOf(
            osmQuest(ApplicableQuestType, NODE, 1),
            osmQuest(ComplexQuestTypeApplicableToNode42, NODE, 1),
            osmQuest(ApplicableQuestType, NODE, 2)
        )

        val keys = listOf(ElementKey(NODE, 1), ElementKey(NODE, 2))
        on(db.getAllForElements(eq(keys))).thenReturn(quests)

        val deleted = listOf(
            ElementKey(NODE, 1),
            ElementKey(NODE, 2)
        )

        mapDataListener.onUpdated(MutableMapDataWithGeometry(), deleted)

        val expectedDeletedQuestKeys = quests.map { it.key }

        verify(db).deleteAll(argThat { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) })
        verify(db).putAll(argThat { it.isEmpty() })
        verify(listener).onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestKeys = argThat { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) }
        )
    }

    @Test fun `calls onInvalidated when cleared quests`() {
        mapDataListener.onCleared()
        verify(db).clear()
        verify(listener).onInvalidated()
    }

    @Test fun `updates quests on map data listener update for updated elements`() {
        val geom = pGeom(0.0, 0.0)

        val elements = listOf(
            node(1, tags = mapOf("a" to "b")),
            // missing geometry
            node(2, tags = mapOf("a" to "b")),
        )
        val geometries = listOf(
            ElementGeometryEntry(NODE, 1L, geom)
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)

        val existingApplicableQuest = osmQuest(ApplicableQuestType, NODE, 1)
        val existingNonApplicableQuest = osmQuest(NotApplicableQuestType, NODE, 1)

        val previousQuests = listOf(existingApplicableQuest, existingNonApplicableQuest)

        on(db.getAllForElements(eq(elements.map { ElementKey(it.type, it.id) }))).thenReturn(previousQuests)
        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        mapDataListener.onUpdated(mapData, emptyList())

        // not testing the intricacies of createQuestsForElement because that is already covered by the
        // unhideAll tests

        val expectedCreatedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geom),
            OsmQuest(ApplicableQuestType2, NODE, 1, geom),
        )

        val expectedDeletedQuestKeys = listOf(existingNonApplicableQuest.key)

        verify(db).deleteAll(eq(expectedDeletedQuestKeys))
        verify(db).putAll(eq(expectedCreatedQuests))
        verify(listener).onUpdated(
            addedQuests = eq(expectedCreatedQuests),
            deletedQuestKeys = eq(expectedDeletedQuestKeys)
        )
    }

    @Test fun `updates quests on map data listener replace for bbox`() {
        val elements = listOf(
            node(1),
            // missing geometry
            node(2),
            // hidden for ApplicableQuestType2
            node(3),
            // at note position
            node(4),
        )
        val geom = pGeom(0.0, 0.0)
        val notePos = p(0.5, 0.5)
        val notePosGeom = ElementPointGeometry(notePos)

        val geometries = listOf(
            ElementGeometryEntry(NODE, 1, geom),
            ElementGeometryEntry(NODE, 3, geom),
            ElementGeometryEntry(NODE, 4, ElementPointGeometry(notePos)),
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)
        val bbox = bbox()

        val existingApplicableQuest = osmQuest(ApplicableQuestType, NODE, 1, geom)
        val existingNonApplicableQuest = osmQuest(NotApplicableQuestType, NODE, 1, geom)

        val previousQuests = listOf(existingApplicableQuest, existingNonApplicableQuest)

        on(db.getAllInBBox(bbox)).thenReturn(previousQuests)

        on(notesSource.getAllPositions(any())).thenReturn(listOf(notePos))

        on(hiddenDB.getAllIds()).thenReturn(listOf(
            OsmQuestKey(NODE, 3L, "ApplicableQuestType2")
        ))

        mapDataListener.onReplacedForBBox(bbox, mapData)

        val expectedAddedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geom),
            OsmQuest(ApplicableQuestType2, NODE, 1, geom),
            OsmQuest(ApplicableQuestType, NODE, 3, geom),
        )

        val expectedCreatedQuests = expectedAddedQuests + listOf(
            OsmQuest(ApplicableQuestType2, NODE, 3, geom),
            OsmQuest(ApplicableQuestType, NODE, 4, notePosGeom),
            OsmQuest(ApplicableQuestType2, NODE, 4, notePosGeom),
        )

        val expectedDeletedQuestKeys = listOf(existingNonApplicableQuest.key)

        verify(db).deleteAll(eq(expectedDeletedQuestKeys))
        verify(db).putAll(argThat { it.containsExactlyInAnyOrder(expectedCreatedQuests) })
        verify(listener).onUpdated(
            addedQuests = argThat { it.containsExactlyInAnyOrder(expectedAddedQuests) },
            deletedQuestKeys = eq(expectedDeletedQuestKeys)
        )
    }
}

private fun questEntry(
    elementType: ElementType = NODE,
    elementId: Long = 1,
    questTypeName: String = "ApplicableQuestType",
    position: LatLon = p()
): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(elementType, elementId, questTypeName, position)

private object ApplicableQuestType : TestQuestTypeA() {
    override fun isApplicableTo(element: Element) = true
}

private object ApplicableQuestType2 : TestQuestTypeA() {
    override fun isApplicableTo(element: Element) = true
}

private object ApplicableQuestTypeNotInAnyCountry : TestQuestTypeA() {
    override fun isApplicableTo(element: Element) = true
    override val enabledInCountries: Countries get() = NoCountriesExcept()
}

private object NotApplicableQuestType : TestQuestTypeA() {
    override fun isApplicableTo(element: Element) = false
}

private object ComplexQuestTypeApplicableToNode42 : TestQuestTypeA() {
    override fun isApplicableTo(element: Element): Boolean? = null
    override fun getApplicableElements(mapData: MapDataWithGeometry) = listOf(node(42))
}
