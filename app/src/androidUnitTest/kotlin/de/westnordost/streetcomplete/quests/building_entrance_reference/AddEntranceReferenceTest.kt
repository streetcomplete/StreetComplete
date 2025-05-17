package de.westnordost.streetcomplete.quests.building_entrance_reference

import de.westnordost.streetcomplete.data.osm.mapdata.ElementType
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.quests.TestMapDataWithGeometry
import de.westnordost.streetcomplete.testutils.member
import de.westnordost.streetcomplete.testutils.node
import de.westnordost.streetcomplete.testutils.rel
import de.westnordost.streetcomplete.testutils.way
import kotlin.test.Test
import kotlin.test.assertEquals

class AddEntranceReferenceTest {

    private val questType = AddEntranceReference()

    @Test
    fun `applicable to plausible entrances`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(2, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(3),
                node(4),
                node(10),
                node(20),
                way(10L, listOf(1, 2, 3, 4), mapOf("building" to "apartments")),
                way(1L, listOf(1, 10), mapOf("highway" to "footway")),
                way(2L, listOf(2, 20), mapOf("highway" to "footway")),
            ),
        )
        assertEquals(2, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable only to buildings with multiple entrances, multiple entrances in entire dataset does not count`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(2),
                node(3),
                node(4),
                node(10),
                node(5, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(6),
                node(7),
                node(8),
                node(20),
                way(100L, listOf(1, 2, 3, 4), mapOf("building" to "apartments")),
                way(101L, listOf(5, 6, 7, 8), mapOf("building" to "apartments")),
                way(200L, listOf(1, 10), mapOf("highway" to "footway")),
                way(201L, listOf(5, 20), mapOf("highway" to "footway")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable in private housing complexes`() {
        // for example in https://www.openstreetmap.org/node/10246306485
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(2, LatLon(50.0, 20.01), mapOf("entrance" to "staircase")),
                node(3),
                node(4),
                node(10),
                node(20),
                way(10L, listOf(1, 2, 3, 4), mapOf("building" to "apartments")),
                way(1L, listOf(1, 10), mapOf("highway" to "footway", "access" to "private")),
                way(2L, listOf(2, 20), mapOf("highway" to "footway", "access" to "no")),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `applicable to multipolygon buildings`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(2, LatLon(50.0, 20.01), mapOf("entrance" to "staircase")),
                node(3),
                node(4),
                way(2L, listOf(1, 2, 3)),
                way(3L, listOf(3, 4, 1)),
                rel(1L, listOf(member(ElementType.WAY, 2), member(ElementType.WAY, 3)), mapOf(
                    "building" to "apartments",
                    "type" to "multipolygon"
                )),
            ),
        )
        assertEquals(2, questType.getApplicableElements(mapData).toList().size)
    }

    @Test
    fun `not applicable to multipolygon building relations`() {
        val mapData = TestMapDataWithGeometry(
            listOf(
                node(1, LatLon(50.0, 20.0), mapOf("entrance" to "staircase")),
                node(2, LatLon(50.0, 20.01), mapOf("entrance" to "staircase")),
                node(3),
                node(4),
                way(2L, listOf(1, 2, 3)),
                way(3L, listOf(3, 4, 1)),
                rel(1L, listOf(member(ElementType.WAY, 2), member(ElementType.WAY, 3)), mapOf(
                    "building" to "apartments",
                )),
            ),
        )
        assertEquals(0, questType.getApplicableElements(mapData).toList().size)
    }
}
