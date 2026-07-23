package de.westnordost.streetcomplete.quests.crossing_island

import de.westnordost.streetcomplete.testutils.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AddCrossingIslandTest {
    private val questType = AddCrossingIsland()

    @Test fun `not applicable to non-crossing`() {
        val node = node(tags = mapOf("nub" to "dub"))
        val mapData = TestMapDataWithGeometry(listOf(node))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(node))
    }

    @Test fun `applicable to crossing`() {
        val crossing = node(tags = mapOf(
            "highway" to "crossing",
            "crossing" to "something"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with private road`() {
        val crossing = node(id = 1, tags = mapOf(
            "highway" to "crossing",
            "crossing" to "something"
        ))
        val privateRoad = way(nodes = listOf(1, 2, 3), tags = mapOf(
            "highway" to "residential",
            "access" to "private"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing, privateRoad))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with adjacent footway = traffic_island`() {
        val crossing = node(id = 2, tags = mapOf(
            "highway" to "crossing",
            "crossing" to "something"
        ))
        val crossingWay = way(id = 1, nodes = listOf(1, 2, 3), tags = mapOf(
            "highway" to "footway",
            "footway" to "crossing"
        ))
        val adjacentTrafficIslandWay = way(id = 2, nodes = listOf(3, 4, 5), tags = mapOf(
            "highway" to "footway",
            "footway" to "traffic_island"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing, crossingWay, adjacentTrafficIslandWay))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }
}
