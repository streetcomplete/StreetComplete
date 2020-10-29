package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.MapDataApi
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.verify
import java.util.concurrent.FutureTask
import javax.inject.Provider

class OsmApiQuestDownloaderTest {
    private lateinit var elementDb: MergedElementDao
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var notePositionsSource: NotePositionsSource
    private lateinit var mapDataApi: MapDataApi
    private lateinit var mapDataWithGeometry: CachingMapDataWithGeometry
    private lateinit var elementGeometryCreator: ElementGeometryCreator
    private lateinit var downloader: OsmApiQuestDownloader

    private val bbox = BoundingBox(0.0, 0.0, 1.0, 1.0)

    @Before fun setUp() {
        elementDb = mock()
        osmQuestController = mock()
        on(osmQuestController.replaceInBBox(any(), any(), any())).thenReturn(OsmQuestController.UpdateResult(0,0))
        countryBoundaries = mock()
        mapDataApi = mock()
        mapDataWithGeometry = mock()
        elementGeometryCreator = mock()
        notePositionsSource = mock()
        val countryBoundariesFuture = FutureTask { countryBoundaries }
        countryBoundariesFuture.run()
        val mapDataProvider = Provider { mapDataWithGeometry }
        downloader = OsmApiQuestDownloader(
            elementDb, osmQuestController, countryBoundariesFuture, notePositionsSource, mapDataApi,
            mapDataProvider, elementGeometryCreator)
    }

    @Test fun `creates quest for element`() {
        val pos = OsmLatLon(1.0, 1.0)
        val node = OsmNode(5, 0, pos, null)
        val geom = ElementPointGeometry(pos)
        val questType = TestMapDataQuestType(listOf(node))

        on(mapDataWithGeometry.getNodeGeometry(5)).thenReturn(geom)
        on(osmQuestController.replaceInBBox(any(), any(), any())).thenAnswer {
            val createdQuests = it.arguments[0] as List<OsmQuest>
            assertEquals(1, createdQuests.size)
            val quest = createdQuests[0]
            assertEquals(5, quest.elementId)
            assertEquals(Element.Type.NODE, quest.elementType)
            assertEquals(geom, quest.geometry)
            assertEquals(questType, quest.osmElementQuestType)
            OsmQuestController.UpdateResult(1,0)
        }

        downloader.download(listOf(questType), bbox)

        verify(elementDb).putAll(any())
        verify(elementDb).deleteUnreferenced()
        verify(osmQuestController).replaceInBBox(any(), any(), any())
    }
}

private class TestMapDataQuestType(private val list: List<Element>) : OsmElementQuestType<String> {

    override var enabledInCountries: Countries = AllCountries

    override val icon = 0
    override val commitMessage = ""
    override fun getTitle(tags: Map<String, String>) = 0
    override fun createForm() = object : AbstractQuestAnswerFragment<String>() {}
    override fun isApplicableTo(element: Element) = false
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> = list
}
