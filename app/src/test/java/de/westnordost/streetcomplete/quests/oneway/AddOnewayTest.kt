package de.westnordost.streetcomplete.quests.oneway

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.MutableMapData
import de.westnordost.osmapi.map.data.OsmWay
import de.westnordost.osmapi.map.data.Way
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.elementgeometry.ElementPointGeometry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class AddOnewayTest {
    private lateinit var questType: AddOneway

    @Before fun setUp() {
        questType = AddOneway()
    }

    @Test fun `does not apply to element without tags`() {
        val mapData = createMapData(noDeadEndWays(null))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `applies to slim road`() {
        val mapData = createMapData(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "1"
        )))
        assertEquals(1, questType.getApplicableElements(mapData).size)
    }

    @Test fun `does not apply to wide road`() {
        val mapData = createMapData(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "5",
            "lanes" to "1"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `applies to wider road that has parking lanes`() {
        val mapData = createMapData(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "12",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        assertEquals(1, questType.getApplicableElements(mapData).size)
    }

    @Test fun `does not apply to wider road that has parking lanes but not enough`() {
        val mapData = createMapData(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "13",
            "lanes" to "1",
            "parking:lane:both" to "perpendicular",
            "parking:lane:both:perpendicular" to "on_street"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `does not apply to slim road with more than one lane`() {
        val mapData = createMapData(noDeadEndWays(mapOf(
            "highway" to "residential",
            "width" to "4",
            "lanes" to "2"
        )))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `does not apply to dead end road #1`() {
        val mapData = createMapData(listOf(
            way(1,listOf(1,2), mapOf("highway" to "residential")),
            way(2,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            ))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `does not apply to dead end road #2`() {
        val mapData = createMapData(listOf(
            way(1,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(2,listOf(3,4), mapOf("highway" to "residential"))
        ))
        assertEquals(0, questType.getApplicableElements(mapData).size)
    }

    @Test fun `applies to road that ends as an intersection in another`() {
        val mapData = createMapData(listOf(
            way(1,listOf(1,2), mapOf("highway" to "residential")),
            way(2,listOf(2,3), mapOf(
                "highway" to "residential",
                "width" to "4",
                "lanes" to "1"
            )),
            way(3,listOf(5,3,4), mapOf("highway" to "residential"))
        ))
        assertEquals(1, questType.getApplicableElements(mapData).size)
    }

    private fun createMapData(ways: List<Way>) : MapDataWithGeometry {
        val mapData = object : MutableMapData(), MapDataWithGeometry {
            override fun getNodeGeometry(id: Long): ElementPointGeometry? = null
            override fun getWayGeometry(id: Long): ElementGeometry?  = null
            override fun getRelationGeometry(id: Long): ElementGeometry?  = null
        }
        mapData.addAll(ways)
        return mapData
    }

    private fun way(id: Long, nodeIds: List<Long>, tags: Map<String, String>?) = OsmWay(id,1, nodeIds, tags)

    private fun noDeadEndWays(tags: Map<String, String>?): List<Way> = listOf(
        way(1,listOf(1,2), mapOf("highway" to "residential")),
        way(2,listOf(2,3), tags),
        way(3,listOf(3,4), mapOf("highway" to "residential"))
    )
}
