package de.westnordost.streetcomplete.data.osm.osmquests

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.data.*
import de.westnordost.streetcomplete.data.osm.edits.MapDataWithEditsSource
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometryEntry
import de.westnordost.streetcomplete.data.osm.geometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MutableMapDataWithGeometry
import de.westnordost.streetcomplete.data.osmnotes.edits.NotesWithEditsSource
import de.westnordost.streetcomplete.data.quest.*
import de.westnordost.streetcomplete.ktx.containsExactlyInAnyOrder
import de.westnordost.streetcomplete.testutils.*
import de.westnordost.streetcomplete.util.enclosingBoundingBox
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask

class OsmQuestControllerTest {

    private lateinit var db: OsmQuestDao
    private lateinit var hiddenDB: OsmQuestsHiddenDao
    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var notesSource: NotesWithEditsSource
    private lateinit var questTypeRegistry: QuestTypeRegistry
    private lateinit var countryBoundaries: CountryBoundaries

    private lateinit var ctrl: OsmQuestController
    private lateinit var listener: OsmQuestSource.Listener
    private lateinit var hideListener: OsmQuestController.HideOsmQuestListener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener
    private lateinit var notesListener: NotesWithEditsSource.Listener

    @Before fun setUp() {
        db = mock()
        // assign ids to added quests
        on(db.addAll(any())).then { invocation ->
            val quests = invocation.getArgument<List<OsmQuest>>(0)
            for (q in quests) {
                q.id = q.elementId
            }
            quests.size
        }

        hiddenDB = mock()
        mapDataSource = mock()

        notesSource = mock()
        questTypeRegistry = QuestTypeRegistry(listOf(
            ApplicableQuestType, NotApplicableQuestType, ComplexQuestTypeApplicableToNode42,
            ApplicableQuestTypeNotInAnyCountry, ApplicableQuestType2
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

        val futureTask = FutureTask { countryBoundaries }
        futureTask.run()

        listener = mock()
        hideListener = mock()
        ctrl = OsmQuestController(db, hiddenDB, mapDataSource, notesSource, questTypeRegistry, futureTask)
        ctrl.addListener(listener)
        ctrl.addHideQuestsListener(hideListener)
    }

    private fun setUpMapDataSource(vararg data: Pair<Element, ElementGeometry?>) {
        for ((element, geom) in data) {
            on(mapDataSource.get(element.type, element.id)).thenReturn(element)

            if (geom != null)
                on(mapDataSource.getGeometry(element.type, element.id)).thenReturn(geom)
        }

        val geomEntries = data.mapNotNull { (element, geom) ->
            geom?.let { ElementGeometryEntry(element.type, element.id, geom)  }
        }

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(MutableMapDataWithGeometry(
            elements = data.map { it.first },
            geometryEntries = geomEntries
        ))
    }

    @Test fun getAllInBBoxCount() {
        val bbox = bbox()
        on(db.getAllInBBoxCount(bbox)).thenReturn(123)
        assertEquals(123, ctrl.getAllInBBoxCount(bbox))
    }

    @Test fun get() {
        val entry = questEntry(123, elementType = Element.Type.NODE, elementId = 1)
        val g = ElementPointGeometry(p(0.0,0.0))

        on(db.get(123)).thenReturn(entry)
        on(mapDataSource.getGeometry(Element.Type.NODE, 1)).thenReturn(g)

        val expectedQuest = OsmQuest(123, ApplicableQuestType, Element.Type.NODE, 1, g)
        assertEquals(expectedQuest, ctrl.get(123))
    }

    @Test fun getAllVisibleInBBox() {
        val entries = listOf(
            // ok!
            questEntry(1, elementType = Element.Type.NODE, elementId = 1),
            // hidden!
            questEntry(2, elementType = Element.Type.NODE, elementId = 2),
            // blacklisted position!
            questEntry(3, elementType = Element.Type.NODE, elementId = 3, position = p(0.5, 0.5)),
            // geometry not found!
            questEntry(4, elementType = Element.Type.NODE, elementId = 4),
        )
        val geoms = listOf(ElementPointGeometry(p()))
        val hiddenQuests = listOf(OsmQuestKey(Element.Type.NODE, 2, "ApplicableQuestType"))
        val bbox = bbox()

        on(hiddenDB.getAllIds()).thenReturn(hiddenQuests)
        on(notesSource.getAllPositions(bbox)).thenReturn(listOf(p(0.5,0.5)))
        on(db.getAllInBBox(bbox, null)).thenReturn(entries)
        on(mapDataSource.getGeometries(argThat {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(Element.Type.NODE, 1),
                ElementKey(Element.Type.NODE, 4),
            ))
        })).thenReturn(listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, geoms[0])
        ))

        val expectedQuests = listOf(
            OsmQuest(1, ApplicableQuestType, Element.Type.NODE, 1, geoms[0]),
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
            OsmQuestKeyWithTimestamp(OsmQuestKey(Element.Type.NODE, 1L, "ApplicableQuestType"), 250),
            // unknown quest type
            OsmQuestKeyWithTimestamp(OsmQuestKey(Element.Type.NODE, 2L, "UnknownQuestType"), 250),
            // no geometry!
            OsmQuestKeyWithTimestamp(OsmQuestKey(Element.Type.NODE, 3L, "ApplicableQuestType"), 250),
        ))
        on(mapDataSource.getGeometries(argThat {
            it.containsExactlyInAnyOrder(listOf(
                ElementKey(Element.Type.NODE, 1),
                ElementKey(Element.Type.NODE, 2),
                ElementKey(Element.Type.NODE, 3)
            ))
        })).thenReturn(listOf(
            ElementGeometryEntry(Element.Type.NODE, 1, geoms[0]),
            ElementGeometryEntry(Element.Type.NODE, 2, geoms[1])
        ))

        assertEquals(
            listOf(
                OsmQuestHidden(Element.Type.NODE, 1, ApplicableQuestType, p(), 250)
            ),
            ctrl.getAllHiddenNewerThan(123L)
        )
    }

    @Test fun hide() {
        val quest = quest(123)

        on(hiddenDB.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())

        ctrl.hide(quest)

        verify(hiddenDB).add(quest.key)
        verify(hideListener).onHid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.osmElementQuestType, quest.position, 555
        )))
        verify(listener).onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestIds = eq(listOf(123L))
        )
    }

    @Test fun unhide() {
        val quest = quest(123)

        on(hiddenDB.delete(quest.key)).thenReturn(true)
        on(hiddenDB.getTimestamp(eq(quest.key))).thenReturn(555)
        on(mapDataSource.getGeometry(quest.elementType, quest.elementId)).thenReturn(pGeom())
        on(db.get(quest.key)).thenReturn(quest)

        assertTrue(ctrl.unhide(quest.key))

        verify(hiddenDB).delete(quest.key)
        verify(hideListener).onUnhid(eq(OsmQuestHidden(
            quest.elementType, quest.elementId, quest.osmElementQuestType, quest.position, 555
        )))
        verify(listener).onUpdated(
            addedQuests = eq(listOf(
                OsmQuest(123, ApplicableQuestType, Element.Type.NODE, 1, pGeom())
            )),
            deletedQuestIds = eq(emptyList())
        )
    }

    @Test fun unhideAll() {
        on(hiddenDB.deleteAll()).thenReturn(2)
        assertEquals(2, ctrl.unhideAll())
        verify(listener).onInvalidated()
        verify(hideListener).onUnhidAll()
    }

    @Test fun `updates quests on notes listener update`() {

        val notes = listOf(note(id = 1, p(1.0,0.0)))

        notesListener.onUpdated(added = notes, updated = emptyList(), deleted = emptyList())

        verify(listener).onInvalidated()
    }

    @Test fun `updates quests on map data listener update for deleted elements`() {
        val quests1 = listOf(
            OsmQuest(10, ApplicableQuestType, Element.Type.NODE, 1, pGeom(1.0,1.0)),
            OsmQuest(20, ComplexQuestTypeApplicableToNode42, Element.Type.NODE, 1, pGeom(1.0,1.0)),
        )
        val quests2 = listOf(
            OsmQuest(30, ApplicableQuestType, Element.Type.NODE, 2, pGeom(2.0,2.0))
        )

        on(db.getAllForElement(Element.Type.NODE, 1)).thenReturn(quests1)
        on(db.getAllForElement(Element.Type.NODE, 2)).thenReturn(quests2)

        val deleted = listOf(
            ElementKey(Element.Type.NODE, 1),
            ElementKey(Element.Type.NODE, 2)
        )

        mapDataListener.onUpdated(MutableMapDataWithGeometry(), deleted)

        verify(db).deleteAll(argThat { it.containsExactlyInAnyOrder(listOf(10, 20, 30)) })
        verify(db).addAll(argThat { it.isEmpty() })
        verify(listener).onUpdated(
            addedQuests = eq(emptyList()),
            deletedQuestIds = argThat { it.containsExactlyInAnyOrder(listOf(10, 20, 30)) }
        )
    }

    @Test fun `updates quests on map data listener update for updated elements`() {

        setUpMapDataSource(
            Pair(node(1), pGeom(1.0,1.0))
        )

        val elements = listOf(
            node(1),
            // missing geometry
            node(2),
        )
        val geometries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1L, pGeom(1.0,1.0))
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)

        val previousQuests = listOf(
            // already exists
            OsmQuest(10, ApplicableQuestType, Element.Type.NODE, 1, pGeom(1.0,1.0)),
            // not applicable
            OsmQuest(20, NotApplicableQuestType, Element.Type.NODE, 1, pGeom(1.0,1.0)),
        )

        on(db.getAllForElement(Element.Type.NODE, 1L)).thenReturn(previousQuests)

        mapDataListener.onUpdated(mapData, emptyList())

        // not testing the intricacies of createQuestsForElement because that is already covered by the
        // unhideAll tests

        val expectedCreatedQuests = listOf(
            OsmQuest(1, ApplicableQuestType2, Element.Type.NODE, 1, pGeom(1.0,1.0)),
        )

        verify(db).deleteAll(eq(listOf(20L)))
        verify(db).addAll(eq(expectedCreatedQuests))
        verify(listener).onUpdated(
            addedQuests = eq(expectedCreatedQuests),
            deletedQuestIds = eq(listOf(20L))
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
        val geometries = listOf(
            ElementGeometryEntry(Element.Type.NODE, 1L, pGeom(1.0,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 3L, pGeom(1.0,1.0)),
            ElementGeometryEntry(Element.Type.NODE, 4L, pGeom(0.5,0.5)),
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)
        val bbox = bbox()

        val previousQuests = listOf(
            // already exists
            OsmQuest(10, ApplicableQuestType, Element.Type.NODE, 1, pGeom(1.0,1.0)),
            // not applicable
            OsmQuest(20, NotApplicableQuestType, Element.Type.NODE, 1, pGeom(1.0,1.0))
        )

        on(db.getAllInBBox(bbox)).thenReturn(previousQuests)

        on(notesSource.getAllPositions(
            p(0.5,0.5).enclosingBoundingBox(1.0)
        )).thenReturn(listOf(p(0.5,0.5)))

        on(hiddenDB.getAllIds()).thenReturn(listOf(
            OsmQuestKey(Element.Type.NODE, 3L, ApplicableQuestType2::class.simpleName!!)
        ))

        mapDataListener.onReplacedForBBox(bbox, mapData)

        val expectedCreatedQuests = listOf(
            OsmQuest(1, ApplicableQuestType2, Element.Type.NODE, 1, pGeom(1.0,1.0)),
            OsmQuest(3, ApplicableQuestType, Element.Type.NODE, 3, pGeom(1.0,1.0)),
            OsmQuest(3, ApplicableQuestType2, Element.Type.NODE, 3, pGeom(1.0,1.0)),
            OsmQuest(4, ApplicableQuestType, Element.Type.NODE, 3, pGeom(1.0,1.0)),
            OsmQuest(4, ApplicableQuestType2, Element.Type.NODE, 3, pGeom(1.0,1.0)),
        )

        val expectedAddedQuests = listOf(
            OsmQuest(1, ApplicableQuestType2, Element.Type.NODE, 1, pGeom(1.0,1.0)),
            OsmQuest(3, ApplicableQuestType, Element.Type.NODE, 3, pGeom(1.0,1.0)),
        )

        verify(db).deleteAll(eq(listOf(20L)))
        verify(db).addAll(any())
        verify(listener).onUpdated(
            addedQuests = argThat { it.containsExactlyInAnyOrder(expectedAddedQuests) },
            deletedQuestIds = eq(listOf(20L))
        )
    }
}

private fun questEntry(
    id: Long? = 1,
    questTypeName: String = "ApplicableQuestType",
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = 1,
    position: LatLon = p()
): OsmQuestDaoEntry = BasicOsmQuestDaoEntry(id, questTypeName, elementType, elementId, position)

private fun quest(
    id: Long? = 1,
    questType: OsmElementQuestType<*> = ApplicableQuestType,
    elementType: Element.Type = Element.Type.NODE,
    elementId: Long = 1,
    geometry: ElementGeometry = pGeom()
) = OsmQuest(id, questType, elementType, elementId, geometry)

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
