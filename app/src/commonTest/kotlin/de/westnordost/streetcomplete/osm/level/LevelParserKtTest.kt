package de.westnordost.streetcomplete.osm.level

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LevelParserKtTest {

    @Test fun `parses level`() {
        assertEquals(listOf(Level.Single(1.0)), "1".toLevelsOrNull())
        assertEquals(listOf(Level.Single(-1.0)), "-1".toLevelsOrNull())
        assertEquals(listOf(Level.Single(+1.0)), "+1".toLevelsOrNull())
        assertEquals(listOf(Level.Single(55.0)), "55".toLevelsOrNull())
        assertEquals(listOf(Level.Single(-0.5)), "-0.5".toLevelsOrNull())
        assertEquals(listOf(Level.Single(5.5)), "5.5".toLevelsOrNull())
        assertNull("".toLevelsOrNull())
        assertNull("6+".toLevelsOrNull())
        assertEquals(listOf(Level.Range(0.0, 10.0)), "+0-10".toLevelsOrNull())
        assertEquals(listOf(Level.Range(0.0, 10.0)), "10-+0".toLevelsOrNull())
        assertEquals(listOf(Level.Range(-1.0, 1.0)), "1--1".toLevelsOrNull())
    }

    @Test fun `parses levels`() {
        assertEquals(listOf(Level.Single(1.0)), "1".toLevelsOrNull())
        assertEquals(listOf(
            Level.Single(1.0),
            Level.Single(2.0),
            Level.Single(-1.0),
        ), "1;2;-1".toLevelsOrNull())
        assertNull("".toLevelsOrNull())
        assertEquals(listOf(Level.Single(12.0)), "12;a;b".toLevelsOrNull())
        assertNull("a;b".toLevelsOrNull())
    }

    @Test fun intersects() {
        assertTrue(intersects(Level.Single(1.0), Level.Single(1.0)))
        assertTrue(intersects(Level.Single(1.5), Level.Single(1.5)))
        assertFalse(intersects(Level.Single(1.5), Level.Single(1.0)))
        assertFalse(intersects(Level.Single(1.0), Level.Single(1.5)))

        assertTrue(intersects(Level.Single(1.5), Level.Range(0.5, 2.5)))
        assertTrue(intersects(Level.Single(0.5), Level.Range(0.5, 2.5)))
        assertTrue(intersects(Level.Single(2.5), Level.Range(0.5, 2.5)))
        assertFalse(intersects(Level.Single(3.0), Level.Range(0.5, 2.5)))
        assertFalse(intersects(Level.Single(0.0), Level.Range(0.5, 2.5)))

        assertTrue(intersects(Level.Range(0.0, 3.0), Level.Range(3.0, 5.0)))
        assertTrue(intersects(Level.Range(0.0, 3.0), Level.Range(-1.0, 0.0)))
        assertTrue(intersects(Level.Range(0.0, 3.0), Level.Range(1.0, 2.0)))
        assertTrue(intersects(Level.Range(0.0, 3.0), Level.Range(-1.0, 4.0)))

        assertFalse(intersects(Level.Range(0.0, 3.0), Level.Range(3.5, 5.0)))
        assertFalse(intersects(Level.Range(0.0, 3.0), Level.Range(-2.0, -0.5)))
    }

    // check if it is symmetrical
    private fun intersects(level1: Level, level2: Level): Boolean {
        val result1 = level1.intersects(level2)
        val result2 = level2.intersects(level1)
        assertEquals(result1, result2)
        return result1
    }
}
