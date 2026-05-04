package de.westnordost.streetcomplete.data.osm.geometry

import PriorityQueue
import de.westnordost.streetcomplete.data.osm.geometry.polygons.Cell
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PriorityQueueTest {

    @Test
    fun testPriorityQueueSortsByCellMaxDescending() {
        val q = PriorityQueue<Cell>()

        // distance = distance to polygon center; half=half size
        val c1 = Cell(0.0, 0.0, 1.0, 1.0) // max = 1 + 1.414
        val c2 = Cell(0.0, 0.0, 1.0, 5.0) // max = 5 + 1.414
        val c3 = Cell(0.0, 0.0, 1.0, 3.0) // max = 3 + 1.414

        q.add(c1)
        q.add(c2)
        q.add(c3)

        // Should extract in descending max order (c2 > c3 > c1)
        assertEquals(c2, q.poll())
        assertEquals(c3, q.poll())
        assertEquals(c1, q.poll())
        assertTrue(q.isEmpty)
    }
}
