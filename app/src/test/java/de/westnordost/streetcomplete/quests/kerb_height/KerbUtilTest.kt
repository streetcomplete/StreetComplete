package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import org.junit.Assert.*
import org.junit.Test

class KerbUtilTest {
    @Test fun `free-floating kerbs do not count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "barrier" to "kerb"
            ))
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `barrier=kerb nodes on footways count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(2L, 1, 0.0,0.0, mapOf(
                "barrier" to "kerb"
            )),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "footway"
            ))
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `intersection nodes between barrier=kerb ways and footways count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(2L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "footway"
            )),
            OsmWay(2L, 1, listOf(4,2,5), mapOf(
                "barrier" to "kerb"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `nodes are not returned twice`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, mapOf(
                "barrier" to "kerb"
            )),
            OsmWay(1L, 1, listOf(1,2,3), mapOf(
                "highway" to "footway"
            )),
            OsmWay(2L, 1, listOf(1,4,5), mapOf(
                "highway" to "footway"
            )),
            OsmWay(2L, 1, listOf(1,6,7), mapOf(
                "barrier" to "kerb"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }
}