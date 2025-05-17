package de.westnordost.streetcomplete.data.user.achievements

import kotlin.test.Test
import kotlin.test.assertEquals

class AchievementTest {
    @Test fun `getPointThreshold for level 0 is 0`() {
        assertEquals(0, achievement { 100 }.getPointThreshold(0))
    }

    @Test fun `getPointThreshold with linear progression`() {
        assertEquals(100, achievement { 10 }.getPointThreshold(10))
    }

    @Test fun `getPointThreshold with other progression`() {
        val a = achievement { it + 1 }
        assertEquals(1, a.getPointThreshold(1))
        assertEquals(3, a.getPointThreshold(2))
        assertEquals(6, a.getPointThreshold(3))
        assertEquals(10, a.getPointThreshold(4))
        assertEquals(15, a.getPointThreshold(5))
        assertEquals(21, a.getPointThreshold(6))
        assertEquals(28, a.getPointThreshold(7))
        assertEquals(36, a.getPointThreshold(8))
    }

    private fun achievement(func: (Int) -> Int): Achievement =
        Achievement("abc", 0, 0, 0, DaysActive, func, mapOf(), -1)
}
