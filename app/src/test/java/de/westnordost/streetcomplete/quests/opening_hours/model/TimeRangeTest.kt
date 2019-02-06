package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import java.util.Locale

import org.junit.Assert.*

class TimeRangeTest {

    @Test fun intersect() {
        val tr = TimeRange(10, 14)
        val directlyAfter = TimeRange(14, 16)
        val clearlyAfter = TimeRange(17, 18)
        val directlyBefore = TimeRange(8, 10)
        val clearlyBefore = TimeRange(4, 8)
        val within = TimeRange(11, 12)
        val intersectsLowerSection = TimeRange(6, 12)
        val intersectUpperSection = TimeRange(12, 20)
        val loopsOutside = TimeRange(20, 4)
        val loopsInside = TimeRange(20, 12)

        assertTrue(tr.intersects(tr))
        assertFalse(tr.intersects(directlyAfter))
        assertFalse(tr.intersects(clearlyAfter))
        assertFalse(tr.intersects(directlyBefore))
        assertFalse(tr.intersects(clearlyBefore))
        assertTrue(tr.intersects(within))
        assertTrue(tr.intersects(intersectsLowerSection))
        assertTrue(tr.intersects(intersectUpperSection))
        assertFalse(tr.intersects(loopsOutside))
        assertTrue(tr.intersects(loopsInside))
    }

    @Test fun `intersection with open end`() {
        val openEnd = TimeRange(10, 50, true)
        val before = TimeRange(0, 5)
        val after = TimeRange(60, 70)
        assertTrue(openEnd.intersects(after))
        assertFalse(openEnd.intersects(before))

        assertFalse(before.intersects(openEnd))
        assertTrue(after.intersects(openEnd))
    }

    @Test fun `toString works`() {
        val openEnd = TimeRange(10, 80, true)
        assertEquals("00:10-01:20+", openEnd.toString())
        assertEquals("00:10 till 01:20+", openEnd.toStringUsing(Locale.US, " till "))
        assertEquals("00:00+", TimeRange(0, 0, true).toString())
    }
}
