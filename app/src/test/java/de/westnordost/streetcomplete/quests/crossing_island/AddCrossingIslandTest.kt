package de.westnordost.streetcomplete.quests.crossing_island

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class AddCrossingIslandTest {
    private val questType = AddCrossingIsland()

    @Test fun `applicable to crossing`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "highway" to "crossing",
                "crossing" to "something"
            ))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
    }

    @Test fun `not applicable to crossing with private road`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "highway" to "crossing",
                "crossing" to "something"
            )),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "residential",
                "access" to "private"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}