package de.westnordost.streetcomplete.data.atp.atpquests

import de.westnordost.streetcomplete.data.atp.AtpEntry
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
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.quest.OsmCreateElementQuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.user.UserDataSource
import de.westnordost.streetcomplete.data.user.UserLoginSource
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement
import de.westnordost.streetcomplete.testutils.any
import de.westnordost.streetcomplete.testutils.atpEntry
import de.westnordost.streetcomplete.testutils.bbox
import de.westnordost.streetcomplete.testutils.mock
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.on
import de.westnordost.streetcomplete.testutils.pGeom
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
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
    private lateinit var userDataSource: UserDataSource
    private lateinit var userLoginSource: UserLoginSource
    private lateinit var prefs: Preferences
    private lateinit var registry: QuestTypeRegistry

    private lateinit var ctrl: AtpQuestController
    private lateinit var listener: AtpQuestSource.Listener

    private lateinit var userLoginListener: UserLoginSource.Listener
    private lateinit var atpUpdatesListener: AtpDataWithEditsSource.Listener

    private lateinit var mapDataListener: MapDataWithEditsSource.Listener

    @BeforeTest
    fun setUp() {
        mapDataSource = mock()
        atpDataSource = mock()
        userDataSource = mock()
        userLoginSource = mock()
        prefs = mock()
        registry = QuestTypeRegistry(listOf(
            0 to MockQuestType
        ))

        listener = mock()

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

        ctrl = AtpQuestController(mapDataSource, atpDataSource, registry)
        ctrl.addListener(listener)
    }

    @Test
    fun `get missing returns null`() {
        on(atpDataSource.get(1)).thenReturn(null)
        assertNull(ctrl.get(1))
    }

    @Test
    fun `getAll created quests if map data is empty and does not obstruct it`() {
        val bbox = bbox()
        val location = LatLon(1.0, 1.0)
        val atpEntries = listOf(
            atpEntry(1, location, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP),
            atpEntry(2, location, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP),
            atpEntry(3, location, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP),
        )

        on(atpDataSource.getAll(bbox)).thenReturn(atpEntries)

        val expectedQuests = atpEntries.map {
            CreateElementUsingAtpQuest(
                it.id, it,
                MockQuestType,
                location
            )
        }

        val mapData = mock<MapDataWithGeometry>()
        val elementList = listOf<Element>()
        on(mapData.iterator()).thenReturn(elementList.iterator())
        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
        )
    }

    @Test
    fun `getAllInBBox skips quest blocked by nearby matching items, does not far away or mismatching ones`() {
        val bbox = bbox()
        val locationForAtpAndMatchingOsmItem = LatLon(11.0, 12.0)
        val locationForAtpAndNotMatchingOsmItem = LatLon(1.0, 1.0)
        val farAwayLocationForOsmItem = LatLon(2.0, 2.0)
        val farAwayLocationFromMatchingOsmItem = LatLon(2.0, 3.0)
        val tagsForAtpAndMatchingOsm = mapOf("name" to "\$NAME", "shop" to "convenience")
        val tagsForAtpNotMatchingOsm = mapOf("name" to "Gęśla Jaźń", "shop" to "bakery")
        val tagsForOsmNotMatchingAtp = mapOf("name" to "Gęśla Jaźń", "shop" to "car_parts")
        val atpEntries = listOf(
            atpEntry(1, locationForAtpAndMatchingOsmItem, tagsInATP=tagsForAtpAndMatchingOsm, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP),
            atpEntry(2, locationForAtpAndNotMatchingOsmItem, tagsInATP=tagsForAtpNotMatchingOsm, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP),
            atpEntry(3, farAwayLocationFromMatchingOsmItem, tagsInATP=tagsForAtpAndMatchingOsm, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        )

        on(atpDataSource.getAll(bbox)).thenReturn(atpEntries)

        val expectedQuests = listOf<CreateElementUsingAtpQuest>(
            CreateElementUsingAtpQuest(
                id = atpEntries[1].id,
                atpEntry = atpEntries[1],
                type = MockQuestType,
                position = atpEntries[1].position,
            ),
            CreateElementUsingAtpQuest(
                id = atpEntries[2].id,
                atpEntry = atpEntries[2],
                type = MockQuestType,
                position = atpEntries[2].position,
            ),
        )

        val mapData = mock<MapDataWithGeometry>()
        val elementMatchingAtp = node(100, locationForAtpAndMatchingOsmItem, tags = tagsForAtpAndMatchingOsm)
        val elementMatchingAtpButAway = node(101, farAwayLocationForOsmItem, tags = tagsForAtpAndMatchingOsm)
        val elementNotMatchingAtp = node(102, locationForAtpAndMatchingOsmItem, tags = tagsForOsmNotMatchingAtp)
        val elementList = listOf<Element>(elementMatchingAtp, elementMatchingAtpButAway, elementNotMatchingAtp)
        on(mapData.iterator()).thenReturn(elementList.iterator())
        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)
        on(mapDataSource.getGeometry(elementMatchingAtp.type, elementMatchingAtp.id))
            .thenReturn(ElementPointGeometry(elementMatchingAtp.position))
        on(mapDataSource.getGeometry(elementMatchingAtpButAway.type, elementMatchingAtpButAway.id))
            .thenReturn(ElementPointGeometry(elementMatchingAtpButAway.position))
        on(mapDataSource.getGeometry(elementNotMatchingAtp.type, elementNotMatchingAtp.id))
            .thenReturn(ElementPointGeometry(elementMatchingAtpButAway.position))

        assertEquals(
            expectedQuests,
            ctrl.getAllInBBox(bbox)
        )
    }

    // is onCleared still needed? it got copied from notes test and interface TODO LATER
    @Test
    fun `calls onInvalidated when cleared entries`() {
        //atpUpdatesListener.onCleared()
        //verify(listener).onInvalidated()
    }

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

    @Test
    fun `new AllThePlaces entries cause quest creation`() {
        val elementList = listOf<Element>()
        val emptyMapData = mock<MapDataWithGeometry>()
        on(emptyMapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(emptyMapData)

        val entry = atpEntry(reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)
        val expectedQuests = listOf<CreateElementUsingAtpQuest>(CreateElementUsingAtpQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()
        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }


    fun dataSetupForAtpEntryFetchedAmongExistingData(atpPos: LatLon, osmPos: LatLon, atpTags: Map<String, String>, osmTags: Map<String, String>, atpEntryId: Long = 10L): AtpEntry {
        val element = node(1, osmPos, tags = osmTags)
        val elementList = listOf<Element>(element)
        val mapData = mock<MapDataWithGeometry>()
        on(mapData.iterator()).thenReturn(elementList.iterator())

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)
        on(mapDataSource.getGeometry(element.type, element.id)).thenReturn(ElementPointGeometry(osmPos))

        val entry = atpEntry(position = atpPos, tagsInATP = atpTags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        return entry
    }

    @Test
    fun `new AllThePlaces entries with matching shop already results in no quest`() {
        val pos = LatLon(20.0, 40.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "supermarket")

        val entry = dataSetupForAtpEntryFetchedAmongExistingData(pos, pos, atpTags, osmTags)

        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)
        verify(listener, never()).onUpdated(anyList(), anyList())
    }


    @Test
    fun `new AllThePlaces entries with already present matching nearby items get no quest`() {
        val pos = LatLon(20.0, 40.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "supermarket")

        val entry = dataSetupForAtpEntryFetchedAmongExistingData(pos, pos, atpTags, osmTags)

        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)

        verify(listener, never()).onUpdated(anyList(), anyList())
    }

    @Test
    fun `new AllThePlaces entry with already present matching far-away item gets quest`() {
        val osmPos = LatLon(20.0, 40.0)
        val atpPos = LatLon(20.0, 41.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "supermarket")

        val entry = dataSetupForAtpEntryFetchedAmongExistingData(osmPos, atpPos, atpTags, osmTags)

        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)
        val expectedQuests = listOf<CreateElementUsingAtpQuest>(CreateElementUsingAtpQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()

        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }


    @Test
    fun `new AllThePlaces entry arriving by update with mismatching nearby items creates quest`() {
        val pos = LatLon(20.0, 40.0)
        val osmTags = mapOf("shop" to "supermarket")
        val atpTags = mapOf("shop" to "hairdresser")

        val entry = dataSetupForAtpEntryFetchedAmongExistingData(pos, pos, atpTags, osmTags)

        val added = listOf(entry)
        val deleted = listOf<Long>()
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)
        val expectedQuests = listOf<CreateElementUsingAtpQuest>(CreateElementUsingAtpQuest(entry.id, entry,
            MockQuestType, entry.position))
        val expectedDeletedIds = listOf<Long>()

        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }

    @Test
    fun `deleted AllThePlaces entry causes quest to be marked for deletion`() {
        val added = listOf<AtpEntry>()
        val deleted = listOf<Long>(10L)
        atpUpdatesListener.onUpdatedAtpElements(added, deleted)
        val expectedQuests = listOf<CreateElementUsingAtpQuest>()
        val expectedDeletedIds = listOf<Long>(10L)
        verify(listener).onUpdated(expectedQuests, expectedDeletedIds)
    }

    @Test
    fun `newly mapped POI near ATP quest causes it to disappear as it matches`() {
        val pos = LatLon(0.0, 10.0)
        val geom = pGeom(pos.latitude, pos.longitude)
        val tags = mapOf("shop" to "foobar")
        val geometryElement = ElementGeometryEntry(NODE, 1L, geom)
        val geometries = listOf(
            geometryElement
        )
        val elements = listOf(
            node(geometryElement.elementId, tags = tags),
        )
        val mapData = MutableMapDataWithGeometry(elements, geometries)

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)
        on(mapDataSource.getGeometry(geometryElement.elementType, geometryElement.elementId)).thenReturn(ElementPointGeometry(pos))

        val entry = atpEntry(position = pos, tagsInATP = tags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        on(atpDataSource.getAll(any())).thenReturn(listOf(entry))

        mapDataListener.onUpdated(mapData, emptyList())

        verify(listener).onUpdated(emptyList(), listOf(entry.id))
    }


    fun dataSetupForOnReplacedForBBox(atpPos: LatLon, osmPos: LatLon, atpTags: Map<String, String>, osmTags: Map<String, String>, atpEntryId: Long): MutableMapDataWithGeometry {
        val entry = atpEntry(atpEntryId, position = atpPos, tagsInATP = atpTags, reportType = ReportType.MISSING_POI_IN_OPENSTREETMAP)
        on(atpDataSource.getAll(any())).thenReturn(listOf(entry))

        val geom = pGeom(osmPos.latitude, osmPos.longitude)
        val geometryElement = ElementGeometryEntry(NODE, atpEntryId, geom)
        val geometries = listOf(
            geometryElement
        )
        val elements = listOf(
            node(geometryElement.elementId, tags = osmTags),
        )
        val mapData = MutableMapDataWithGeometry(elements, geometries)

        on(mapDataSource.getMapDataWithGeometry(any())).thenReturn(mapData)
        on(mapDataSource.getGeometry(geometryElement.elementType, geometryElement.elementId)).thenReturn(ElementPointGeometry(osmPos))
        return mapData
    }

    @Test
    fun `onReplacedForBBox has matching data near existing ATP quest and causes it to disappear`() {
        val pos = LatLon(0.0, 10.0)
        val atpTags = mapOf("shop" to "foobar")
        val osmTags = mapOf("shop" to "foobar")
        val atpEntryId = 10L

        val mapData = dataSetupForOnReplacedForBBox(pos, pos, atpTags, osmTags, atpEntryId)
        val bbox = pos.enclosingBoundingBox(300.0)
        mapDataListener.onReplacedForBBox(bbox, mapData)

        verify(listener).onUpdated(emptyList(), listOf(atpEntryId))
    }

    @Test
    fun `onReplacedForBBox has not matching data near existing ATP quest and does not cause it to disappear`() {
        val pos = LatLon(0.0, 10.0)
        val atpTags = mapOf("shop" to "foobar")
        val osmTags = mapOf("shop" to "some_other_value")
        val atpEntryId = 10L

        val mapData = dataSetupForOnReplacedForBBox(pos, pos, atpTags, osmTags, atpEntryId)
        val bbox = pos.enclosingBoundingBox(300.0)
        mapDataListener.onReplacedForBBox(bbox, mapData)

        verify(listener, never()).onUpdated(anyList(), anyList())
    }

    @Test
    fun `onReplacedForBBox has matching data far away from existing ATP quest and is not causing it to disappear`() {
        val atpPos = LatLon(0.0, 10.0)
        val osmPos = LatLon(0.0, 40.0)
        val atpTags = mapOf("shop" to "foobar")
        val osmTags = mapOf("shop" to "foobar")
        val atpEntryId = 10L

        val mapData = dataSetupForOnReplacedForBBox(atpPos, osmPos, atpTags, osmTags, atpEntryId)
        val bbox = osmPos.enclosingBoundingBox(6_000_000.0)
        mapDataListener.onReplacedForBBox(bbox, mapData)

        verify(listener, never()).onUpdated(anyList(), anyList())
    }
}

object MockQuestType : OsmCreateElementQuestType<String> {
    override val icon: Int = 199
    override val title: Int = 199
    override val wikiLink: String? = null
    override val achievements: List<EditTypeAchievement> = mock()
    override val changesetComment: String = "changeset comment from MockQuestType"
}
