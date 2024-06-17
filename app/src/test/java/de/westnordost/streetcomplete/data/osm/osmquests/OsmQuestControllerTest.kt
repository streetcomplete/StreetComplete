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
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.note
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.testutils.verifyInvokedExactlyOnce
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import io.mockative.Mock
import io.mockative.any
import io.mockative.classOf
import io.mockative.eq
import io.mockative.every
import io.mockative.matches
import io.mockative.mock
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OsmQuestControllerTest {

    @Mock private lateinit var db: OsmQuestDao
    @Mock private lateinit var hiddenDB: OsmQuestsHiddenDao
    @Mock private lateinit var mapDataSource: MapDataWithEditsSource
    @Mock private lateinit var notesSource: NotesWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    // todo
    private lateinit var countryBoundaries: CountryBoundaries

    private lateinit var ctrl: OsmQuestController
    @Mock private lateinit var listener: OsmQuestSource.Listener
    @Mock private lateinit var hideListener: OsmQuestsHiddenSource.Listener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener
    private lateinit var notesListener: NotesWithEditsSource.Listener

    @BeforeTest fun setUp() {
        db = mock(classOf<OsmQuestDao>())

        hiddenDB = mock(classOf<OsmQuestsHiddenDao>())
        mapDataSource = mock(classOf<MapDataWithEditsSource>())

        notesSource = mock(classOf<NotesWithEditsSource>())
        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to ApplicableQuestType,
            1 to NotApplicableQuestType,
            2 to ComplexQuestTypeApplicableToNode42,
            3 to ApplicableQuestTypeNotInAnyCountry,
            4 to ApplicableQuestType2
        ))
        countryBoundaries = mock(classOf<CountryBoundaries>())

        every { mapDataSource.addListener(any()) }.invokes { arguments ->
            mapDataListener = arguments[0] as MapDataWithEditsSource.Listener
            Unit
        }

        every { notesSource.addListener(any()) }.invokes { arguments ->
            notesListener = arguments[0] as NotesWithEditsSource.Listener
            Unit
        }

        listener = mock(classOf<OsmQuestSource.Listener>())
        hideListener = mock(classOf<OsmQuestsHiddenSource.Listener>())
        ctrl = OsmQuestController(db, hiddenDB, mapDataSource, notesSource, questTypeRegistry, lazyOf(countryBoundaries))
        ctrl.addListener(listener)
        ctrl.addListener(hideListener)
    }

    @Test fun get() {
        val key = osmQuestKey(NODE, 1, "ApplicableQuestType")
        val entry = questEntry(NODE, 1, "ApplicableQuestType")
        val g = pGeom()

        every { db.get(osmQuestKey(NODE, 1, "ApplicableQuestType")) }.returns(entry)
        every { mapDataSource.getGeometry(NODE, 1) }.returns(g)

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

        every { hiddenDB.getAllIds() }.returns(hiddenQuests)
        every { notesSource.getAllPositions(any()) }.returns(listOf(notePos))
        every { db.getAllInBBox(bbox, null) }.returns(entries)
        every { mapDataSource.getGeometries(matches {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(NODE, 1),
                ElementKey(NODE, 4),
            ))
        })}.returns(listOf(
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

        every { hiddenDB.getNewerThan(123L) }.returns(listOf(
            // ok!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 1L, "ApplicableQuestType"), 250),
            // unknown quest type
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 2L, "UnknownQuestType"), 250),
            // no geometry!
            OsmQuestKeyWithTimestamp(OsmQuestKey(NODE, 3L, "ApplicableQuestType"), 250),
        ))
        every { mapDataSource.getGeometries(matches {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(NODE, 1),
                ElementKey(NODE, 2),
                ElementKey(NODE, 3)
            ))
        })}.returns(listOf(
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
        every { hiddenDB.countAll() }.returns(123L)
        assertEquals(123L, ctrl.countAll())
    }

    @Test fun hide() {
        val quest = osmQuest(questType = ApplicableQuestType)

        every { hiddenDB.getTimestamp(eq(quest.key)) }.returns(555)
        every { mapDataSource.getGeometry(quest.elementType, quest.elementId) }.returns(pGeom())

        ctrl.hide(quest.key)

        verifyInvokedExactlyOnce { hiddenDB.add(quest.key) }
        verifyInvokedExactlyOnce { hideListener.onHid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.type, quest.position, 555
        )))}
        verifyInvokedExactlyOnce { listener.onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestKeys = eq(listOf(quest.key))
        )}
    }

    @Test fun unhide() {
        val quest = osmQuest(questType = ApplicableQuestType)

        every { hiddenDB.delete(quest.key) }.returns(true)
        every { hiddenDB.getTimestamp(eq(quest.key)) }.returns(555)
        every { mapDataSource.getGeometry(quest.elementType, quest.elementId) }.returns(pGeom())
        every { db.get(quest.key) }.returns(quest)

        assertTrue(ctrl.unhide(quest.key))

        verifyInvokedExactlyOnce { hiddenDB.delete(quest.key) }
        verifyInvokedExactlyOnce { hideListener.onUnhid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.type, quest.position, 555
        )))}
        verifyInvokedExactlyOnce { listener.onUpdated(
            addedQuests = eq(listOf(quest)),
            deletedQuestKeys = eq(emptyList())
        )}
    }

    @Test fun unhideAll() {
        every { hiddenDB.deleteAll() }.returns(2)
        assertEquals(2, ctrl.unhideAll())
        verifyInvokedExactlyOnce { listener.onInvalidated() }
        verifyInvokedExactlyOnce { hideListener.onUnhidAll() }
    }

    @Test fun `updates quests on notes listener update`() {
        val notes = listOf(note(1))

        notesListener.onUpdated(added = notes, updated = emptyList(), deleted = emptyList())

        verifyInvokedExactlyOnce { listener.onInvalidated() }
    }

    @Test fun `updates quests on map data listener update for deleted elements`() {
        val quests = listOf(
            osmQuest(ApplicableQuestType, NODE, 1),
            osmQuest(ComplexQuestTypeApplicableToNode42, NODE, 1),
            osmQuest(ApplicableQuestType, NODE, 2)
        )

        val keys = listOf(ElementKey(NODE, 1), ElementKey(NODE, 2))
        every { db.getAllForElements(eq(keys)) }.returns(quests)

        val deleted = listOf(
            ElementKey(NODE, 1),
            ElementKey(NODE, 2)
        )

        mapDataListener.onUpdated(MutableMapDataWithGeometry(), deleted)

        val expectedDeletedQuestKeys = quests.map { it.key }

        verifyInvokedExactlyOnce { db.deleteAll(matches { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) }) }
        verifyInvokedExactlyOnce { db.putAll(matches { it.isEmpty() }) }
        verifyInvokedExactlyOnce { listener.onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestKeys = matches { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) }
        )}
    }

    @Test fun `calls onInvalidated when cleared quests`() {
        mapDataListener.onCleared()
        verifyInvokedExactlyOnce { db.clear() }
        verifyInvokedExactlyOnce { listener.onInvalidated() }
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

        every { db.getAllForElements(eq(elements.map { ElementKey(it.type, it.id) })) }.returns(previousQuests)
        every { mapDataSource.getMapDataWithGeometry(any()) }.returns(mapData)

        mapDataListener.onUpdated(mapData, emptyList())

        // not testing the intricacies of createQuestsForElement because that is already covered by the
        // unhideAll tests

        val expectedCreatedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geom),
            OsmQuest(ApplicableQuestType2, NODE, 1, geom),
        )

        val expectedDeletedQuestKeys = listOf(existingNonApplicableQuest.key)

        verifyInvokedExactlyOnce { db.deleteAll(eq(expectedDeletedQuestKeys)) }
        verifyInvokedExactlyOnce { db.putAll(eq(expectedCreatedQuests)) }
        verifyInvokedExactlyOnce { listener.onUpdated(
            addedQuests = eq(expectedCreatedQuests),
            deletedQuestKeys = eq(expectedDeletedQuestKeys)
        )}
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

        every { db.getAllInBBox(bbox) }.returns(previousQuests)

        every { notesSource.getAllPositions(any()) }.returns(listOf(notePos))

        every { hiddenDB.getAllIds() }.returns(listOf(
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

        verifyInvokedExactlyOnce { db.deleteAll(eq(expectedDeletedQuestKeys)) }
        verifyInvokedExactlyOnce { db.putAll(matches { it.containsExactlyInAnyOrder(expectedCreatedQuests) }) }
        verifyInvokedExactlyOnce { listener.onUpdated(
            addedQuests = matches { it.containsExactlyInAnyOrder(expectedAddedQuests) },
            deletedQuestKeys = eq(expectedDeletedQuestKeys)
        )}
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
