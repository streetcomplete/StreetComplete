package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.ReportType
import de.westnordost.streetcomplete.data.atp.atpquests.edits.AtpDataWithEditsSource
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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtp
import de.westnordost.streetcomplete.data.quest.atp.CreatePoiBasedOnAtpAnswer
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.atpEntry
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.eq
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AtpQuestControllerTest {

    private lateinit var mapDataSource: MapDataWithEditsSource
    private lateinit var atpDataSource: AtpDataWithEditsSource
    private lateinit var noteSource: NotesWithEditsSource
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences
    private lateinit var registry: QuestTypeRegistry

    private lateinit var ctrl: AtpQuestController
    private lateinit var listener: AtpQuestSource.Listener

    private lateinit var noteUpdatesListener: NotesWithEditsSource.Listener
    private lateinit var userLoginListener: UserLoginSource.Listener
    private lateinit var atpUpdatesListener: AtpDataWithEditsSource.Listener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener

    @BeforeTest
    fun setUp() {
        noteSource = mock()
        mapDataSource = mock()
        atpDataSource = mock()
        userDataSource = mock()
        userLoginSource = mock()
        prefs = mock()
        registry = QuestTypeRegistry(listOf(
            0 to MockQuestType
        ))

        listener = mock()

        on(noteSource.addListener(any())).then { invocation ->
            noteUpdatesListener = invocation.getArgument(0)
            Unit
        }

        on(userLoginSource.addListener(any())).then { invocation ->
            userLoginListener = invocation.getArgument(0)
            Unit
        }

        on(atpDataSource.addListener(any())).then { invocation ->
            atpUpdatesListener = invocation.getArgument(0)
            Unit
        }

        on(mapDataSource.addListener(any())).then { invocation ->
            mapDataListener = invocation.getArgument(0)
            Unit
        }

        ctrl = AtpQuestController(mapDataSource, atpDataSource, noteSource, registry)
        ctrl.addListener(listener)
    }

    // TODO: adapt for ATP or delete
    @Test
    fun `get missing returns null`() {
        on(noteSource.get(1)).thenReturn(null)
        assertNull(ctrl.get(1))
    }

    @Test
    fun getAll() {
        val bbox = bbox()
        val location = LatLon(1.0, 1.0)
        val atpEntries = listOf(atpEntry(1, location), atpEntry(2, location), atpEntry(3, location))

        on(atpDataSource.getAll(bbox)).thenReturn(atpEntries)

        val expectedQuests = atpEntries.map {
            CreateElementQuest(
                it.id, it,
                CreatePoiBasedOnAtp(), location
            )
        }

        val mapData = mock<MapDataWithGeometry>()
        val mockElement1 = mock<Element>()
        val mockElement2 = mock<Element>()
        val elementList = listOf<Element>() // mockElement1, mockElement2
        on(mapData.iterator()).thenReturn(elementList.iterator())
        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
        )
    }

    // is onCleared still needed? it got copied from notes test and interface TODO
    @Test
    fun `calls onInvalidated when cleared entries`() {
        noteUpdatesListener.onCleared()
        verify(listener).onInvalidated()
    }

    // TODO: adapt for ATP or delete (seems reasonable to test? but both updated notes and main onUpdated needs to be tested)
    /*
    @Test fun `calls onUpdated when notes changed`() {
        on(prefs.showAllNotes).thenReturn(true)

        noteUpdatesListener.onUpdated(
            added = listOf(note(1)),
            updated = listOf(note(2)),
            deleted = listOf(3)
        )

        verify(listener).onUpdated(
            added = argThat {
                it.containsExactlyInAnyOrder(listOf(OsmNoteQuest(1, p()), OsmNoteQuest(2, p())))
            },
            deleted = argThat { it.containsExactlyInAnyOrder(listOf(3)) }
        )
    }
    */

    @Test
    fun `isThereOsmAtpMatch matches on exact copies`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertTrue(
            ctrl.isThereOsmAtpMatch(mapOf("name" to "Aldi", "shop" to "supermarket"), mapOf("name" to "Aldi", "shop" to "supermarket"),
                ElementKey(ElementType.NODE, 1),
                LatLon(0.0, 0.0)
            )
        )
    }

    @Test
    fun `isThereOsmAtpMatch matches on rejected due to large distance`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertFalse(
            ctrl.isThereOsmAtpMatch(mapOf("name" to "Aldi", "shop" to "supermarket"), mapOf("name" to "Aldi", "shop" to "supermarket"),
                ElementKey(ElementType.NODE, 1),
                LatLon(1.0, 0.0)
            )
        )
    }

    @Test
    fun `isThereOsmAtpMatch matches despite capitalization difference`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertTrue(
            ctrl.isThereOsmAtpMatch(mapOf("name" to "ALDI"), mapOf("name" to "Aldi"),
            ElementKey(ElementType.NODE, 1),
            LatLon(0.0, 0.0)
            )
        )
    }

    @Test
    fun `isThereOsmAtpMatch rejects matches when nothing matches`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertFalse(
            ctrl.isThereOsmAtpMatch(
                mapOf("name" to "Foobar", "shop" to "convenience"),
                mapOf("name" to "Platypus", "shop" to "trade"),
                ElementKey(ElementType.NODE, 1),
                LatLon(0.0, 0.0)
            )
        )
    }

    @Test
    fun `isThereOsmAtpMatch allows matches between similar shop types`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertTrue(
            ctrl.isThereOsmAtpMatch(
                mapOf("name" to "Tesco", "shop" to "convenience"),
                mapOf("name" to "Tesco", "shop" to "supermarket"),
                ElementKey(ElementType.NODE, 1),
                LatLon(0.0, 0.0)
            )
        )
    }

    /*
    TODO: implement such improvements
    @Test fun `isThereOsmAtpMatch does not detect match on completely mismatching names, also if type matches`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertFalse(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Foobar", "shop" to "convenience"),
            mapOf("name" to "Platypus", "shop" to "convenience")),
            ElementKey(ElementType.NODE, 1),
            LatLon(0.0, 0.0)
        )
    }

    @Test fun `isThereOsmAtpMatch allows matches between similar shop types, also with subtle name differences`() {
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).then {
            val returned = mock<ElementPointGeometry>()
            on(returned.center).thenReturn(LatLon(0.0, 0.0))
            returned
        }
        assertFalse(ctrl.isThereOsmAtpMatch(
            mapOf("name" to "Tesco", "shop" to "convenience"),
            mapOf("name" to "Tesco #123", "shop" to "supermarket")),
            ElementKey(ElementType.NODE, 1),
            LatLon(0.0, 0.0)
        )
    }
     */
    // TODO: see https://codeberg.org/matkoniecz/list_how_openstreetmap_can_be_improved_with_alltheplaces_data/src/branch/master/test_matching_logic.py for possible extension

    @Test
    fun `new AllThePlaces entries cause quest creation`() {
        val elementList = listOf<Element>()
        val emptyMapData = mock<MapDataWithGeometry>()
        on(emptyMapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(emptyMapData)

        val entry = atpEntry(reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElement(added, deleted)
        val expectedQuests = listOf<CreateElementQuest>(CreateElementQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()
        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }

    @Test
    fun `AllThePlaces entries with matching nearby items get no quest`() {
        val pos = LatLon(20.0, 40.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "supermarket")

        val elementList = listOf<Element>(node(1, pos, tags = osmTags))
        val mapData = mock<MapDataWithGeometry>()
        on(mapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)
        on(mapDataSource.getGeometry(ElementType.NODE, 1)).thenReturn(ElementPointGeometry(pos))

        val entry = atpEntry(position = pos, tagsInATP = atpTags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElement(added, deleted)

        verify(listener, never()).onUpdated(anyList(), anyList())
    }

    @Test
    fun `AllThePlaces entries with matching far-away item gets quest`() {
        val osmPos = LatLon(20.0, 40.0)
        val atpPos = LatLon(20.0, 41.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "supermarket")

        val elementList = listOf<Element>(node(1, osmPos, tags = osmTags))
        val mapData = mock<MapDataWithGeometry>()
        on(mapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        val entry = atpEntry(position = atpPos, tagsInATP = atpTags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElement(added, deleted)
        val expectedQuests = listOf<CreateElementQuest>(CreateElementQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()

        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }

    @Test
    fun `AllThePlaces entries with mismatching nearby items gets quest`() {
        val pos = LatLon(20.0, 40.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "hairdresser")

        val elementList = listOf<Element>(node(1, pos, tags = osmTags))
        val mapData = mock<MapDataWithGeometry>()
        on(mapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        val entry = atpEntry(position = pos, tagsInATP = atpTags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElement(added, deleted)
        val expectedQuests = listOf<CreateElementQuest>(CreateElementQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()

        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }

    @Test
    fun `newly mapped POI near ATP quest causes it to disappear if it matches`() { // TODO - implement

        val pos = LatLon(0.0, 10.0)
        val geom = pGeom(pos.latitude, pos.longitude)
        val tags = mapOf("shop" to "foobar")
        val elements = listOf(
            node(1, tags = tags),
        )
        val geometries = listOf(
            ElementGeometryEntry(NODE, 1L, geom)
        )

        val mapData = MutableMapDataWithGeometry(elements, geometries)

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        val entry = atpEntry(position = pos, tagsInATP = tags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        on(atpDataSource.getAll(any())).thenReturn(listOf(entry))

        mapDataListener.onUpdated(mapData, emptyList())

        verify(listener).onUpdated(emptyList(), listOf(entry.id))
    }

    @Test
    fun `new AllThePlaces entries with matching shop already results in no quest`() { // TODO - implement
        // onUpdatedAtpElement

        val bbox = bbox()
        val location = LatLon(1.0, 1.0)
        val atpEntries = listOf(atpEntry(1, location), atpEntry(2, location), atpEntry(3, location))

        on(atpDataSource.getAll(bbox)).thenReturn(atpEntries)
        val mapData = mock<MapDataWithGeometry>()
        val mockElement1 = mock<Element>()
        val mockElement2 = mock<Element>()
        val elementList = listOf<Element>() // mockElement1, mockElement2
        on(mapData.iterator()).thenReturn(elementList.iterator())
        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

    }

    @Test
    fun `new map data cause quest creation`() { // TODO - implement
    }
}

object MockQuestType : OsmCreateElementQuestType<String> {
    override val icon: Int = 199
    override val title: Int = 199
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = mock()
    override val changesetComment: String = "changeset comment from MockQuestType"
}
