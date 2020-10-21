package de.westnordost.streetcomplete.data.osm.osmquest

import de.westnordost.countryboundaries.CountryBoundaries
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometryCreator
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.MergedElementDao
import de.westnordost.streetcomplete.data.osmnotes.NotePositionsSource
import de.westnordost.streetcomplete.data.quest.AllCountries
import de.westnordost.streetcomplete.data.quest.AllCountriesExcept
import de.westnordost.streetcomplete.data.quest.Countries
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import java.util.concurrent.FutureTask


class OsmQuestDownloaderTest {
    private lateinit var elementDb: MergedElementDao
    private lateinit var osmQuestController: OsmQuestController
    private lateinit var countryBoundaries: CountryBoundaries
    private lateinit var notePositionsSource: NotePositionsSource
    private lateinit var elementGeometryCreator: ElementGeometryCreator
    private lateinit var elementEligibleForOsmQuestChecker: ElementEligibleForOsmQuestChecker
    private lateinit var downloader: OsmQuestDownloader

    @Before fun setUp() {
        elementDb = mock()
        osmQuestController = mock()
        on(osmQuestController.replaceInBBox(any(), any(), any())).thenReturn(OsmQuestController.UpdateResult(0,0))
        countryBoundaries = mock()
        elementGeometryCreator = mock()
        notePositionsSource = mock()
        elementEligibleForOsmQuestChecker = mock()
        val countryBoundariesFuture = FutureTask { countryBoundaries }
        countryBoundariesFuture.run()
        downloader = OsmQuestDownloader(elementDb, osmQuestController, countryBoundariesFuture, notePositionsSource, elementEligibleForOsmQuestChecker)
    }

    @Test fun `ignore element with invalid geometry`() {
        val questType = TestDownloaderQuestType(mapOf(
            OsmNode(0, 0, OsmLatLon(1.0, 1.0), null) to
                    null
        ))

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0))
    }

    @Test fun `ignore at blacklisted position`() {
        val blacklistPos = OsmLatLon(0.3, 0.4)
        on(notePositionsSource.getAllPositions(any())).thenReturn(listOf(blacklistPos))

        val questType = TestDownloaderQuestType(mapOf(
            OsmNode(0, 0, blacklistPos, null) to
                    ElementPointGeometry(blacklistPos)
        ))

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0))
    }

    @Test fun `ignore element in country for which this quest is disabled`() {
        val pos = OsmLatLon(1.0, 1.0)

        val questType = TestDownloaderQuestType(mapOf(
            OsmNode(0, 0, pos, null) to
                    ElementPointGeometry(pos)
        ))
        questType.enabledInCountries = AllCountriesExcept("AA")
        // country boundaries say that position is in AA
        on(countryBoundaries.isInAny(anyDouble(),anyDouble(),any())).thenReturn(true)
        on(countryBoundaries.getContainingIds(anyDouble(),anyDouble(),anyDouble(),anyDouble())).thenReturn(setOf())

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0))
    }

    @Test fun `creates quest for element`() {
        val pos = OsmLatLon(1.0, 1.0)

        val questType = TestDownloaderQuestType(mapOf(
            OsmNode(0, 0, pos, null) to
                    ElementPointGeometry(pos)
        ))

        on(osmQuestController.replaceInBBox(any(), any(), any())).thenReturn(OsmQuestController.UpdateResult(0,0))

        downloader.download(questType, BoundingBox(0.0, 0.0, 1.0, 1.0))

        verify(elementDb).putAll(any())
        verify(osmQuestController).replaceInBBox(any(), any(), any())
    }
}

private class TestDownloaderQuestType(private val map: Map<Element, ElementGeometry?>) : OsmDownloaderQuestType<String> {

    override var enabledInCountries: Countries = AllCountries

    override fun download(bbox: BoundingBox, handler: (element: Element, geometry: ElementGeometry?) -> Unit): Boolean {
        for ((element, geometry) in map) {
            handler(element, geometry)
        }
        return true
    }

    override val icon = 0
    override val commitMessage = ""
    override fun getTitle(tags: Map<String, String>) = 0
    override fun createForm() = object : AbstractQuestAnswerFragment<String>() {}
    override fun isApplicableTo(element: Element) = false
    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {}
}
