package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.any
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.mock
import de.westnordost.streetcomplete.on
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsNoQuest
import de.westnordost.streetcomplete.quests.verifyDownloadYieldsQuest
import org.junit.Before
import org.junit.Test

class AddOnewayTest {
    private lateinit var overpassMock: OverpassMapDataAndGeometryApi
    private lateinit var questType: AddOneway

    @Before fun setUp() {
        overpassMock = mock()
        questType = AddOneway(overpassMock)
    }

    @Test fun `does not apply to element without tags`() {
        setUpElements(noDeadEndWays(null))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `applies to slim road`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "1"
        )))
        questType.verifyDownloadYieldsQuest(mock())
    }

    @Test fun `does not apply to wide road`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "5",
            "lanes" to "1"
        )))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `applies to wider road that has parking lanes`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "12",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        questType.verifyDownloadYieldsQuest(mock())
    }

    @Test fun `does not apply to wider road that has parking lanes but not enough`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "13",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `applies to wider road that has cycle lanes`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "6",
            "lanes" to "1",
            "cycleway" to "lane"
        )))
        questType.verifyDownloadYieldsQuest(mock())
    }

    @Test fun `does not apply to slim road with more than one lane`() {
        setUpElements(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "2"
        )))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `does not apply to dead end road #1`() {
        setUpElements(listOf(
            way(listOf(1,2), mapOf("highway" to "residential")),
            way(listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            ))
        ))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `does not apply to dead end road #2`() {
        setUpElements(listOf(
            way(listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(listOf(3,4), mapOf("highway" to "residential"))
        ))
        questType.verifyDownloadYieldsNoQuest(mock())
    }

    @Test fun `does not apply to road that ends as an intersection in another`() {
        setUpElements(listOf(
            way(listOf(1,2), mapOf("highway" to "residential")),
            way(listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(listOf(5,3,4), mapOf("highway" to "residential"))
        ))
        questType.verifyDownloadYieldsQuest(mock())
    }

    private fun setUpElements(ways: List<Way>) {
        on(overpassMock.query(any(), any())).then { invocation ->
            val callback = invocation.getArgument(1) as (element: Element, geometry: ElementGeometry?) -> Unit
            for (way in ways) {
                callback(way, null)
            }
            true
        }
    }

    private fun way(nodeIds: List<Long>, tags: Map<String, String>?) = OsmWay(1,1, nodeIds, tags)

    private fun noDeadEndWays(tags: Map<String, String>?): List<Way> = listOf(
        way(listOf(1,2), mapOf("highway" to "residential")),
        way(listOf(2,3), tags),
        way(listOf(3,4), mapOf("highway" to "residential"))
    )
}
