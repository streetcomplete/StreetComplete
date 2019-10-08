package de.westnordost.streetcomplete.data.osm.download

import org.junit.Test


import org.junit.Assert.*

class NodeWayMapTest {
    @Test fun all() {
        val way1 = listOf(1L, 2L, 3L)
        val way2 = listOf(3L, 4L, 1L)
        val ring = listOf(5L, 1L, 6L, 5L)

        val map = NodeWayMap(listOf(way1, way2, ring))

        assertTrue(map.hasNextNode())
        assertEquals(2, map.getWaysAtNode(1L)?.size)
        assertEquals(2, map.getWaysAtNode(3L)?.size)
        assertEquals(2, map.getWaysAtNode(5L)?.size)
        assertNull(map.getWaysAtNode(2L))

        map.removeWay(way1)
        assertEquals(1, map.getWaysAtNode(1L)?.size)
        assertEquals(1, map.getWaysAtNode(3L)?.size)

        map.removeWay(way2)
        assertNull(map.getWaysAtNode(1L))
        assertNull(map.getWaysAtNode(3L))

        assertTrue(map.hasNextNode())
        assertEquals(5L, map.getNextNode())

        map.removeWay(ring)

        assertFalse(map.hasNextNode())
    }
}
