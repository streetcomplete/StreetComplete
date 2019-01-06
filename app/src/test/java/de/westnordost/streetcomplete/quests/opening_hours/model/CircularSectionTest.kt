package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import org.junit.Assert.*

class CircularSectionTest {
    @Test fun `start and end`() {
        val cs = CircularSection(0, 10)
        assertEquals(0, cs.start.toLong())
        assertEquals(10, cs.end.toLong())
        assertFalse(cs.loops)
    }

    @Test fun loops() {
        val cs = CircularSection(10, 0)
        assertTrue(cs.loops)
    }

    @Test fun intersect() {
        val cs = CircularSection(0, 10)
        val tooHigh = CircularSection(11, 12)
        val tooLow = CircularSection(11, 12)
        val touchesUpperEnd = CircularSection(10, 10)
        val touchesLowerEnd = CircularSection(-1, 0)
        val contains = CircularSection(-10, 20)
        val intersectsLowerSection = CircularSection(-10, 3)
        val intersectUpperSection = CircularSection(8, 20)
        val loopsOutside = CircularSection(11, -1)
        val loopsIntersectsLowerSection = CircularSection(11, 3)
        val loopsIntersectsUpperSection = CircularSection(8, -5)

        assertTrue(cs.intersects(cs))
        assertFalse(cs.intersects(tooHigh))
        assertFalse(cs.intersects(tooLow))
        assertTrue(cs.intersects(touchesLowerEnd))
        assertTrue(cs.intersects(touchesUpperEnd))
        assertTrue(cs.intersects(contains))
        assertTrue(cs.intersects(intersectsLowerSection))
        assertTrue(cs.intersects(intersectUpperSection))
        assertFalse(cs.intersects(loopsOutside))
        assertTrue(cs.intersects(loopsIntersectsLowerSection))
        assertTrue(cs.intersects(loopsIntersectsUpperSection))
        assertTrue(loopsIntersectsLowerSection.intersects(loopsIntersectsUpperSection))
    }

    @Test fun compare() {
        val looper = CircularSection(10, 0)
        val lowStart = CircularSection(0, 10)
        val lowStartButHighEnd = CircularSection(0, 50)
        val highStart = CircularSection(10, 20)

        assertTrue(looper < lowStart)
        assertTrue(looper < lowStartButHighEnd)
        assertTrue(looper < highStart)
        assertTrue(lowStart > looper)
        assertTrue(lowStartButHighEnd > looper)
        assertTrue(highStart > looper)

        assertTrue(lowStart < lowStartButHighEnd)
        assertTrue(lowStart < highStart)
        assertTrue(lowStartButHighEnd > lowStart)
        assertTrue(highStart > lowStart)

        assertTrue(lowStartButHighEnd < highStart)
        assertTrue(highStart > lowStartButHighEnd)
    }

    @Test fun toStringUsing() {
        val abc = arrayOf("a", "b", "c", "d")
        assertEquals("a", CircularSection(0, 0).toStringUsing(abc, "-"))
        assertEquals("a-d", CircularSection(0, 3).toStringUsing(abc, "-"))
        assertEquals("a-b", CircularSection(0, 1).toStringUsing(abc, "-"))
    }

    @Test fun equals() {
        val cs = CircularSection(0, 10)
        assertEquals(cs, cs)
        assertNotEquals(cs, Any())
        assertNotEquals(cs, CircularSection(10, 0))
        assertEquals(cs, CircularSection(0, 10))
    }

    @Test fun `hash code identity`() {
        assertEquals(
            CircularSection(0, 10).hashCode(),
            CircularSection(0, 10).hashCode()
        )
    }

    @Test fun `hash code is not too simple`() {
        assertNotEquals(
            CircularSection(0, 10).hashCode(),
            CircularSection(10, 0).hashCode()
        )
    }
}
