package de.westnordost.streetcomplete.osm

import org.junit.Assert.*
import org.junit.Test

class LevelKtTest {

    @Test fun `parses level`() {
        assertEquals(SingleLevel(1.0), "1".toLevelOrNull())
        assertEquals(SingleLevel(-1.0), "-1".toLevelOrNull())
        assertEquals(SingleLevel(+1.0), "+1".toLevelOrNull())
        assertEquals(SingleLevel(55.0), "55".toLevelOrNull())
        assertEquals(SingleLevel(-0.5), "-0.5".toLevelOrNull())
        assertEquals(SingleLevel(5.5), "5.5".toLevelOrNull())
        assertNull("".toLevelOrNull())
        assertNull("6+".toLevelOrNull())
        assertEquals(LevelRange(0.0, 10.0), "+0-10".toLevelOrNull())
        assertEquals(LevelRange(0.0, 10.0), "10-+0".toLevelOrNull())
        assertEquals(LevelRange(-1.0, 1.0), "1--1".toLevelOrNull())
    }

    @Test fun `parses levels`() {
        assertEquals(listOf(SingleLevel(1.0)), "1".toLevelsOrNull())
        assertEquals(listOf(
            SingleLevel(1.0),
            SingleLevel(2.0),
            SingleLevel(-1.0),
        ), "1;2;-1".toLevelsOrNull())
        assertNull("".toLevelsOrNull())
        assertEquals(listOf(SingleLevel(12.0)), "12;a;b".toLevelsOrNull())
        assertNull("a;b".toLevelsOrNull())
    }

    @Test fun intersects() {
        assertTrue(intersects(SingleLevel(1.0), SingleLevel(1.0)))
        assertTrue(intersects(SingleLevel(1.5), SingleLevel(1.5)))
        assertFalse(intersects(SingleLevel(1.5), SingleLevel(1.0)))
        assertFalse(intersects(SingleLevel(1.0), SingleLevel(1.5)))

        assertTrue(intersects(SingleLevel(1.5), LevelRange(0.5, 2.5)))
        assertTrue(intersects(SingleLevel(0.5), LevelRange(0.5, 2.5)))
        assertTrue(intersects(SingleLevel(2.5), LevelRange(0.5, 2.5)))
        assertFalse(intersects(SingleLevel(3.0), LevelRange(0.5, 2.5)))
        assertFalse(intersects(SingleLevel(0.0), LevelRange(0.5, 2.5)))

        assertTrue(intersects(LevelRange(0.0, 3.0), LevelRange(3.0, 5.0)))
        assertTrue(intersects(LevelRange(0.0, 3.0), LevelRange(-1.0, 0.0)))
        assertTrue(intersects(LevelRange(0.0, 3.0), LevelRange(1.0, 2.0)))
        assertTrue(intersects(LevelRange(0.0, 3.0), LevelRange(-1.0, 4.0)))

        assertFalse(intersects(LevelRange(0.0, 3.0), LevelRange(3.5, 5.0)))
        assertFalse(intersects(LevelRange(0.0, 3.0), LevelRange(-2.0, -0.5)))
    }

    // check if it is symmetrical
    private fun intersects(level1: Level, level2: Level): Boolean {
        val result1 = level1.intersects(level2)
        val result2 = level2.intersects(level1)
        assertEquals(result1, result2)
        return result1
    }
}
