package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.streetcomplete.util.countryboundaries.CountryBoundaries
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
import de.westnordost.streetcomplete.util.countryboundaries.Countries
import de.westnordost.streetcomplete.util.countryboundaries.NoCountriesExcept
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.TestQuestTypeA
import dev.mokkery.matcher.any
import de.westnordost.streetcomplete.testutils.bbox
import dev.mokkery.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.note
import dev.mokkery.answering.returns
import dev.mokkery.every
import de.westnordost.streetcomplete.testutils.osmQuest
import de.westnordost.streetcomplete.testutils.osmQuestKey
import de.westnordost.streetcomplete.testutils.p
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.ktx.containsExactlyInAnyOrder
import dev.mokkery.answering.calls
import dev.mokkery.matcher.matches
import dev.mokkery.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class OsmQuestControllerTest {

    private lateinit var db: OsmQuestDao

    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var notesSource: NotesWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var countryBoundaries: CountryBoundaries

    private lateinit var ctrl: OsmQuestController
    private lateinit var listener: OsmQuestSource.Listener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener
    private lateinit var notesListener: NotesWithEditsSource.Listener

    @BeforeTest fun setUp() {
        db = mock()

        mapDataSource = mock() {
            every { addListener(any()) } calls { (listener: MapDataWithEditsSource.Listener) ->
                mapDataListener = listener
            }
        }

        notesSource = mock() {
            every { addListener(any()) } calls { (listener: NotesWithEditsSource.Listener) ->
                notesListener = listener
            }
        }

        questTypeRegistry = QuestTypeRegistry(listOf(
            0 to ApplicableQuestType,
            1 to NotApplicableQuestType,
            2 to ComplexQuestTypeApplicableToNode42,
            3 to ApplicableQuestTypeNotInAnyCountry,
            4 to ApplicableQuestType2
        ))
        countryBoundaries = mock()

        listener = mock()

        ctrl = OsmQuestController(db, mapDataSource, notesSource, questTypeRegistry, lazyOf(countryBoundaries))
        ctrl.addListener(listener)
    }

    @Test fun get() {
        val key = osmQuestKey(NODE, 1, "ApplicableQuestType")
        val entry = questEntry(NODE, 1, "ApplicableQuestType")
        val g = pGeom()

        every { db.get(key) } returns entry
        every { mapDataSource.getGeometry(NODE, 1) } returns g
        every { notesSource.getAllPositions(any()) } returns listOf()

        val expectedQuest = OsmQuest(ApplicableQuestType, NODE, 1, g)
        assertEquals(expectedQuest, ctrl.get(key))
    }

    @Test fun getAllVisibleInBBox() {
        val notePos = p(0.5, 0.5)
        val entries = listOf(
            // ok!
            questEntry(elementType = NODE, elementId = 1),
            // blacklisted position!
            questEntry(elementType = NODE, elementId = 3, position = notePos),
            // geometry not found!
            questEntry(elementType = NODE, elementId = 4),
        )
        val geoms = listOf(ElementPointGeometry(p()))

        val bbox = bbox()
        every { notesSource.getAllPositions(any()) } returns listOf(notePos)
        every { db.getAllInBBox(bbox, null) } returns entries
        every {
            mapDataSource.getGeometries(matches {
                it.containsExactlyInAnyOrder(listOf(ElementKey(NODE, 1), ElementKey(NODE, 4)))
            })
        } returns listOf(
            ElementGeometryEntry(NODE, 1, geoms[0])
        )

        val expectedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geoms[0]),
        )
        assertTrue(ctrl.getAllInBBox(bbox, null).containsExactlyInAnyOrder(expectedQuests))
    }

    @Test fun `updates quests on notes listener update`() {
        val notes = listOf(note(1))

        notesListener.onUpdated(added = notes, updated = emptyList(), deleted = emptyList())

        verify { listener.onInvalidated() }
    }

    @Test fun `updates quests on map data listener update for deleted elements`() {
        val quests = listOf(
            osmQuest(ApplicableQuestType, NODE, 1),
            osmQuest(ComplexQuestTypeApplicableToNode42, NODE, 1),
            osmQuest(ApplicableQuestType, NODE, 2)
        )

        val deleted = listOf(
            ElementKey(NODE, 1),
            ElementKey(NODE, 2)
        )

        every { db.getAllForElements(emptyList()) } returns emptyList()
        every { db.getAllForElements(deleted) } returns quests

        mapDataListener.onUpdated(MutableMapDataWithGeometry(), deleted)

        val expectedDeletedQuestKeys = quests.map { it.key }

        verify { db.deleteAll(matches { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) }) }
        verify { db.putAll(matches { it.isEmpty() }) }
        verify {
            listener.onUpdated(
                added = emptyList(),
                deleted = matches { it.containsExactlyInAnyOrder(expectedDeletedQuestKeys) }
            )
        }
    }

    @Test fun `calls onInvalidated when cleared quests`() {
        mapDataListener.onCleared()
        verify { db.clear() }
        verify { listener.onInvalidated() }
    }

    @Test fun `updates quests on map data listener update for updated elements`() {
        val geom = pGeom()

        val elements = listOf(
            node(1, tags = mapOf("a" to "b")),
            node(2, tags = mapOf("a" to "b")), // missing geometry
        )
        val geometries = listOf(
            ElementGeometryEntry(NODE, 1L, geom)
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)

        val existingApplicableQuest = osmQuest(ApplicableQuestType, NODE, 1)
        val existingNonApplicableQuest = osmQuest(NotApplicableQuestType, NODE, 1)

        val previousQuests = listOf(existingApplicableQuest, existingNonApplicableQuest)

        every { db.getAllForElements(elements.map { ElementKey(it.type, it.id) }) } returns previousQuests
        every { db.getAllForElements(emptyList()) } returns emptyList()
        every { mapDataSource.getMapDataWithGeometry(any()) } returns mapData

        mapDataListener.onUpdated(mapData, emptyList())

        // not testing the intricacies of createQuestsForElement because that is already covered by the
        // unhideAll tests

        val expectedCreatedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geom),
            OsmQuest(ApplicableQuestType2, NODE, 1, geom),
        )

        val expectedDeletedQuestKeys = listOf(existingNonApplicableQuest.key)

        verify { db.deleteAll(expectedDeletedQuestKeys) }
        verify { db.putAll(expectedCreatedQuests) }
        verify {
            listener.onUpdated(
                added = expectedCreatedQuests,
                deleted = expectedDeletedQuestKeys
            )
        }
    }

    @Test fun `updates quests on map data listener replace for bbox`() {
        val elements = listOf(
            node(1),
            node(2), // missing geometry
            node(4), // at note position
        )
        val geom = pGeom(0.0, 0.0)
        val notePos = p(0.5, 0.5)
        val notePosGeom = ElementPointGeometry(notePos)

        val geometries = listOf(
            ElementGeometryEntry(NODE, 1, geom),
            ElementGeometryEntry(NODE, 4, notePosGeom),
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)
        val bbox = bbox()

        val existingApplicableQuest = osmQuest(ApplicableQuestType, NODE, 1, geom)
        val existingNonApplicableQuest = osmQuest(NotApplicableQuestType, NODE, 1, geom)

        val previousQuests = listOf(existingApplicableQuest, existingNonApplicableQuest)

        every { db.getAllInBBox(bbox) } returns previousQuests

        every { notesSource.getAllPositions(any()) } returns listOf(notePos)

        mapDataListener.onReplacedForBBox(bbox, mapData)

        val expectedAddedQuests = listOf(
            OsmQuest(ApplicableQuestType, NODE, 1, geom),
            OsmQuest(ApplicableQuestType2, NODE, 1, geom),
        )

        val expectedCreatedQuests = expectedAddedQuests + listOf(
            OsmQuest(ApplicableQuestType, NODE, 4, notePosGeom),
            OsmQuest(ApplicableQuestType2, NODE, 4, notePosGeom),
        )

        val expectedDeletedQuestKeys = listOf(existingNonApplicableQuest.key)

        verify { db.deleteAll(expectedDeletedQuestKeys) }
        verify { db.putAll(matches { it.containsExactlyInAnyOrder(expectedCreatedQuests) }) }
        verify {
            listener.onUpdated(
                added = matches { it.containsExactlyInAnyOrder(expectedAddedQuests) },
                deleted = expectedDeletedQuestKeys
            )
        }
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
