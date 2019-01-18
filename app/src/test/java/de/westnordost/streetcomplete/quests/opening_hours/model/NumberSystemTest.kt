package de.westnordost.streetcomplete.quests.opening_hours.model


import org.junit.Test

import org.junit.Assert.*

class NumberSystemTest {
    @Test(expected = IllegalArgumentException::class) fun `illegal arguments`() {
        NumberSystem(10, 3)
    }

    @Test fun getSize() {
        val s = NumberSystem(5, 10)
        assertEquals(5, s.getSize(CircularSection(2, 9)).toLong())
        assertEquals(1, s.getSize(CircularSection(5, 5)).toLong())
        assertEquals(2, s.getSize(CircularSection(5, 6)).toLong())
        assertEquals(6, s.getSize(CircularSection(6, 5)).toLong())
        assertEquals(3, s.getSize(CircularSection(9, 5)).toLong())
    }

    @Test fun `complement none`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf())
        assertEquals(listOf(CircularSection(0, 10)), r)
    }

    @Test fun `complement full`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(0, 10)))
        assertTrue(r.isEmpty())
    }

    @Test fun `complement one at end`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(0, 8)))
        assertEquals(listOf(CircularSection(9, 10)), r)
    }

    @Test fun `complement the very last`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(0, 9)))
        assertEquals(listOf(CircularSection(10, 10)), r)
    }

    @Test fun `complement one at start`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(4, 10)))
        assertEquals(listOf(CircularSection(0, 3)), r)
    }

    @Test fun `complement the very first`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(1, 10)))
        assertEquals(listOf(CircularSection(0, 0)), r)
    }

    @Test fun `complement one in the center`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(0, 3), CircularSection(6, 10)))
        assertEquals(listOf(CircularSection(4, 5)), r)
    }

    @Test fun `complement at both ends`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(CircularSection(3, 8)))
        assertEquals(listOf(CircularSection(9, 2)), r)
    }

    @Test fun `complement swiss cheese`() {
        val s = NumberSystem(0, 10)
        val r = s.complemented(listOf(
            CircularSection(-5, 1),
            CircularSection(3, 3),
            CircularSection(7, 8)
        ))
        assertEquals(3, r.size)
        assertTrue(
            r.containsAll(listOf(
                CircularSection(2, 2),
                CircularSection(4, 6),
                CircularSection(9, 10)
            ))
        )
    }

    @Test fun `complement one that loops`() {
        val s = NumberSystem(0,10)
        val r = s.complemented(listOf(CircularSection(8,5)))
        assertEquals(listOf(CircularSection(6, 7)), r)
    }

    @Test fun `no complement at end`() {
        val s = NumberSystem(0,10)
        val r = s.complemented(listOf(CircularSection(0,9), CircularSection(10,10)))
        assertTrue(r.isEmpty())
    }

    @Test fun merge() {
        val s = NumberSystem(3,10)
        val r = s.merged(listOf(CircularSection(3,4), CircularSection(9,10)))
        assertEquals(listOf(CircularSection(9,4)), r)
    }
}
