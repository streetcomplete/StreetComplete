package de.westnordost.streetcomplete.quests.crossing_island

import de.westnordost.streetcomplete.node
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

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
        val privateRoad = OsmWay(1L, 1, listOf(1,2,3), mapOf(
            "highway" to "residential",
            "access" to "private"
        ))
        val mapData = TestMapDataWithGeometry(listOf(crossing, privateRoad ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }
}
