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

    @Test fun `shared nodes between barrier=kerb ways and footways count`() {
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

    @Test fun `shared endpoints between sidewalks and crossings count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(2L, 1, listOf(1,3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between sidewalks and crossings and sidewalk without endpoint don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(2L, 1, listOf(1,3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            OsmWay(3L, 1, listOf(4,1,5), mapOf(
                "highway" to "footway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between two crossings don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            OsmWay(2L, 1, listOf(1,3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between a crossing and not-endpoints of a sidewalk don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            OsmWay(2L, 1, listOf(4, 1,3), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and several sidewalks don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(2L, 1, listOf(1,4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(3L, 1, listOf(1,3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalks, some not fully tagged, don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(2L, 1, listOf(1,4), mapOf(
                "highway" to "footway",
            )),
            OsmWay(3L, 1, listOf(1,3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalk and cycleway don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            OsmWay(2L, 1, listOf(1,4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(3L, 1, listOf(1,3), mapOf(
                "highway" to "cycleway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalk and footway construction don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            OsmNode(1L, 1, 0.0,0.0, null),
            OsmWay(1L, 1, listOf(1,2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            OsmWay(2L, 1, listOf(1,4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            OsmWay(3L, 1, listOf(1,3), mapOf(
                "construction" to "footway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
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
