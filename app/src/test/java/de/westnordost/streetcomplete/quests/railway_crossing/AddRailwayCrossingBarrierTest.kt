package de.westnordost.streetcomplete.quests.railway_crossing

import de.westnordost.streetcomplete.node
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class AddRailwayCrossingBarrierTest {
    private val questType = AddRailwayCrossingBarrier()

    @Test fun `not applicable to non-crossing`() {
        val node = node(tags = mapOf("plumps" to "didumps"))
        val mapData = TestMapDataWithGeometry(listOf(node))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
        assertEquals(false, questType.isApplicableTo(node))
    }

    @Test fun `applicable to crossing`() {
        val crossing = node(tags = mapOf("railway" to "level_crossing"))
        val mapData = TestMapDataWithGeometry(listOf(crossing))
        assertEquals(1, questType.getApplicableElements(mapData).toList().size)
        assertNull(questType.isApplicableTo(crossing))
    }

    @Test fun `not applicable to crossing with private road`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, tags = mapOf("railway" to "level_crossing")),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "residential",
                "access" to "private"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
