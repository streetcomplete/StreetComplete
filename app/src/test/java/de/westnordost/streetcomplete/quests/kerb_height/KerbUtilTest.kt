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

    // see https://github.com/streetcomplete/StreetComplete/blob/master/res/documentation/kerbs/kerb-node.svg
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

    // see https://github.com/streetcomplete/StreetComplete/blob/master/res/documentation/kerbs/kerb-way.svg
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

    // see https://github.com/streetcomplete/StreetComplete/blob/master/res/documentation/kerbs/crossing-style2.svg
    @Test fun `shared endpoints between sidewalks etc and crossings etc count`() {
        val likeSidewalks = listOf("sidewalk", "traffic_island")
        val likeCrossings = listOf("crossing", "access_aisle")
        for (likeSidewalk in likeSidewalks) {
            for (likeCrossing in likeCrossings) {
                val mapData = TestMapDataWithGeometry(listOf(
                    node(id = 1),
                    way(1, listOf(1, 2), mapOf(
                        "highway" to "footway",
                        "footway" to likeSidewalk
                    )),
                    way(2, listOf(1, 3), mapOf(
                        "highway" to "footway",
                        "footway" to likeCrossing
                    )),
                ))
                assertEquals(1, mapData.findAllKerbNodes().toList().size)
            }
        }
    }

    // see https://github.com/streetcomplete/StreetComplete/blob/master/res/documentation/kerbs/footway-crossing.svg
    // assuming the orange way could be any other way, too
    @Test fun `shared endpoints between sidewalks and crossings and any other way don't count`() {
        val waysTags = listOf(
            mapOf("highway" to "footway", "footway" to "sidewalk"),
            mapOf("highway" to "footway", "footway" to "traffic_island"),
            mapOf("highway" to "footway", "footway" to "crossing"), // yes, even other crossings
            mapOf("highway" to "footway"),
            mapOf("highway" to "cycleway"),
            mapOf("highway" to "path"),
            mapOf("highway" to "bridleway"),
            mapOf("highway" to "service"),
            mapOf("highway" to "residential"),
            mapOf("construction" to "footway"),
            mapOf("construction" to "cycleway"),
            mapOf("construction" to "path"),
            mapOf("construction" to "bridleway"),
            mapOf("construction" to "service"),
            mapOf("construction" to "residential"),
        )

        for (wayTags in waysTags) {
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
                way(3, listOf(1, 5), wayTags),
            ))
            assertEquals(0, mapData.findAllKerbNodes().toList().size)
        }
    }

    @Test fun `shared endpoints between two crossings etc don't count`() {
        val likeCrossings = listOf("crossing", "access_aisle")
        for (likeCrossing in likeCrossings) {
            val mapData = TestMapDataWithGeometry(listOf(
                node(id = 1),
                way(1, listOf(1, 2), mapOf(
                    "highway" to "footway",
                    "footway" to "crossing"
                )),
                way(2, listOf(1, 3), mapOf(
                    "highway" to "footway",
                    "footway" to likeCrossing
                )),
            ))
            assertEquals(0, mapData.findAllKerbNodes().toList().size)
        }
    }

    // see https://github.com/streetcomplete/StreetComplete/blob/master/res/documentation/kerbs/crossing-style1.svg
    // lower side (assuming the lower sidewalk is just one way)
    @Test fun `endpoints of a crossing and non-endpoints of a sidewalk don't count`() {
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
