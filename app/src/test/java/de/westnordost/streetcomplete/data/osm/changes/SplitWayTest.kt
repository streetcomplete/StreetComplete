package de.westnordost.streetcomplete.data.osm.changes

import de.westnordost.osmapi.map.data.OsmNode
import de.westnordost.osmapi.map.data.OsmWay
import org.junit.Test
import java.lang.IllegalArgumentException

class SplitWayTest {

    private val node1 = OsmNode(1, 1, 0.0, 0.0, null)
    private val node2 = OsmNode(2, 1, 0.0, 1.0, null)
    private val node3 = OsmNode(3, 1, 1.0, 1.0, null)
    private val way = OsmWay(1,1, listOf(1,2,3), null)

    @Test(expected = IllegalArgumentException::class)
    fun `do not allow negative delta`() {
        SplitWay(way, node1, node2, -0.1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `do not allow delta above or equal 1`() {
        SplitWay(way, node1, node2, 1.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `do not allow delta 0 if split is at first node of way`() {
        SplitWay(way, node1, node2, 0.0)
    }

    @Test fun `allow delta 0 if split is not at first node of way`() {
        SplitWay(way, node2, node3, 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ensure that first node is actually in the way`() {
        val way = OsmWay(1,1, listOf(2,3), null)
        SplitWay(way, node1, node2, 0.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ensure that second node is actually in the way`() {
        val way = OsmWay(1,1, listOf(1,2), null)
        SplitWay(way, node1, node3, 0.5)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `ensure that second node is one after first node in the way`() {
        SplitWay(way, node1, node3, 0.5)
    }
}
