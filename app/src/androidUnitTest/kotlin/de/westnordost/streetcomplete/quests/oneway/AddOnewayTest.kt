package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.streetcomplete.data.osm.mapdata.Way
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddOnewayTest {
    private val questType = AddOneway()

    @Test fun `does not apply to element without tags`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(emptyMap()))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to slim road`() {
        val mapData = TestMapDataWithGeometry(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4"
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
            way(1, listOf(1, 2), mapOf("highway" to "residential")),
            way(2, listOf(2, 3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `does not apply to dead end road #2`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(2, 3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(2, listOf(3, 4), mapOf("highway" to "residential"))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `applies to road that ends as an intersection in another`() {
        val mapData = TestMapDataWithGeometry(listOf(
            way(1, listOf(1, 2), mapOf("highway" to "residential")),
            way(2, listOf(2, 3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(3, listOf(5, 3, 4), mapOf("highway" to "residential"))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    private fun noDeadEndWays(tags: Map<String, String>): List<Way> = listOf(
        way(1, listOf(1, 2), mapOf("highway" to "residential")),
        way(2, listOf(2, 3), tags),
        way(3, listOf(3, 4), mapOf("highway" to "residential"))
    )
}
