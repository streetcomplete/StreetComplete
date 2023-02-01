package de.westnordost.streetcomplete.quests.kerb_height

import de.westnordost.streetcomplete.osm.kerb.couldBeAKerb
import de.westnordost.streetcomplete.osm.kerb.findAllKerbNodes
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.way
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KerbUtilTest {
    @Test fun `free-floating kerbs do not count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, tags = mapOf("barrier" to "kerb"))
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `barrier=kerb nodes on footways count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 2, tags = mapOf("barrier" to "kerb")),
            way(1, listOf(1, 2, 3), mapOf(
                "highway" to "footway"
            ))
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `barrier=kerb nodes that are also something else don't count`() {
        val kerb = node(id = 2, tags = mapOf(
            "barrier" to "kerb",
            "highway" to "crossing"
        ))
        val mapData = TestMapDataWithGeometry(listOf(
            kerb,
            way(1, listOf(1, 2, 3), mapOf(
                "highway" to "footway"
            ))
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
        assertFalse(kerb.couldBeAKerb())
    }

    @Test fun `barrier=kerb nodes that have kerb key etc count`() {
        val kerb = node(id = 2, tags = mapOf(
            "barrier" to "kerb",
            "kerb" to "lowered",
            "check_date:kerb" to "2001-01-01"
        ))
        val mapData = TestMapDataWithGeometry(listOf(
            kerb,
            way(1, listOf(1, 2, 3), mapOf(
                "highway" to "footway"
            ))
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
        assertTrue(kerb.couldBeAKerb())
    }

    @Test fun `shared nodes between barrier=kerb ways and footways count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 2),
            way(1, listOf(1, 2, 3), mapOf(
                "highway" to "footway"
            )),
            way(2, listOf(4, 2, 5), mapOf(
                "barrier" to "kerb"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between sidewalks and crossings count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between traffic islands and crossings count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "traffic_island"
            )),
            way(2, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between traffic islands and sidewalks don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "traffic_island"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between sidewalks and crossings and sidewalk without endpoint don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(3, listOf(4, 1, 5), mapOf(
                "highway" to "footway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between two crossings don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(2, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between a crossing and not-endpoints of a sidewalk don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(2, listOf(4, 1, 3), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and several sidewalks don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(3, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossing, traffic island and sidewalk don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "traffic_island"
            )),
            way(3, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between two crossings and sidewalk don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(1),
            way(1L, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2L, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(3L, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalks, some not fully tagged, don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(),
            way(1L, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(2L, listOf(1, 4), mapOf(
                "highway" to "footway",
            )),
            way(3L, listOf(1, 3), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalk and cycleway don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(2, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(3, listOf(1, 3), mapOf(
                "highway" to "cycleway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `shared endpoints between crossings and sidewalk and footway construction don't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(2, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(3, listOf(1, 3), mapOf(
                "construction" to "footway",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `intersection with a road doesn't count`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1),
            way(1, listOf(1, 2), mapOf(
                "highway" to "footway",
                "footway" to "crossing"
            )),
            way(2, listOf(1, 4), mapOf(
                "highway" to "footway",
                "footway" to "sidewalk"
            )),
            way(3, listOf(5, 1, 3), mapOf(
                "highway" to "secondary",
            )),
        ))
        assertEquals(0, mapData.findAllKerbNodes().toList().size)
    }

    @Test fun `nodes are not returned twice`() {
        val mapData = TestMapDataWithGeometry(listOf(
            node(id = 1, tags = mapOf("barrier" to "kerb")),
            way(1, listOf(1, 2, 3), mapOf(
                "highway" to "footway"
            )),
            way(2, listOf(1, 4, 5), mapOf(
                "highway" to "footway"
            )),
            way(2, listOf(1, 6, 7), mapOf(
                "barrier" to "kerb"
            )),
        ))
        assertEquals(1, mapData.findAllKerbNodes().toList().size)
    }
}
