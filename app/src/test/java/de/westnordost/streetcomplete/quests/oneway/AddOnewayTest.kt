package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.assertEquals
import org.junit.Test

class AddOnewayTest {
    private val questType = AddOneway()

    @Test fun `does not apply to element without tags`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(null))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to slim road`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "1"
        )))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to wide road`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "5",
            "lanes" to "1"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to wider road that has parking lanes`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "12",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to wider road that has parking lanes but not enough`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "13",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to wider road that has cycle lanes`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "6",
            "lanes" to "1",
            "cycleway" to "lane"
        )))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to slim road with more than one lane`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "2"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to dead end road #1`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1,listOf(1,2), mapOf("highway" to "residential")),
            way(2,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to dead end road #2`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(2,listOf(3,4), mapOf("highway" to "residential"))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to road that ends as an intersection in another`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1,listOf(1,2), mapOf("highway" to "residential")),
            way(2,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(3,listOf(5,3,4), mapOf("highway" to "residential"))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    private fun way(id: Long, nodeIds: List<Long>, tags: Map<String, String>?) = OsmWay(id,1, nodeIds, tags)

    private fun noDeadEndWays(tags: Map<String, String>?): List<Way> = listOf(
        way(1,listOf(1,2), mapOf("highway" to "residential")),
        way(2,listOf(2,3), tags),
        way(3,listOf(3,4), mapOf("highway" to "residential"))
    )
}
