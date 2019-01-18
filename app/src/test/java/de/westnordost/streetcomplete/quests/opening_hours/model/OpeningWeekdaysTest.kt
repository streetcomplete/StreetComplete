package de.westnordost.streetcomplete.quests.opening_hours.model

import org.junit.Test

import org.junit.Assert.*

class OpeningWeekdaysTest {

    private val monday = Weekdays(booleanArrayOf(true))
    private val tuesday = Weekdays(booleanArrayOf(false, true))

    @Test fun `no intersection on different weekday`() {
        assertFalse(intersects(
            days(monday, hours(2, 6)),
            days(tuesday, hours(3, 5))
        ))
    }

    @Test fun `no intersection on same weekday`() {
        assertFalse(intersects(
            days(monday, hours(2, 6)),
            days(monday, hours(8, 10))
        ))
    }

    @Test fun `one intersection`() {
        assertTrue(intersects(
            days(monday, hours(8, 15)),
            days(monday, hours(14, 18))
        ))
    }

    @Test fun `intersection at next day`() {
        assertTrue(intersects(
            days(monday, hours(18, 4)),
            days(tuesday, hours(2, 12))
        ))
    }

    @Test fun `no intersection at next day`() {
        assertFalse(intersects(
            days(monday, hours(18, 4)),
            days(tuesday, hours(12, 20))
        ))
    }

    @Test fun `one intersection at next day amongst several ranges`() {
        assertTrue(intersects(
            days(monday, hours(12, 16), hours(18, 4)),
            days(tuesday, hours(20, 4), hours(2, 12))
        ))
    }

    @Test fun `no intersection at next day amongst several ranges`() {
        assertFalse(intersects(
            days(monday, hours(12, 16), hours(18, 4)),
            days(tuesday, hours(20, 4), hours(4, 12))
        ))
    }

    @Test fun `intersection on same day amongst several ranges`() {
        assertTrue(intersects(
            days(monday, hours(2, 8), hours(8, 10)),
            days(monday, hours(14, 18), hours(9, 12))
        ))
    }

    @Test fun `no weekdays intersection`() {
        assertFalse(intersectsWeekdays(
            days(monday, hours(8, 12)),
            days(tuesday, hours(8, 12))
        ))
    }

    @Test fun `normal weekdays intersection`() {
        assertTrue(intersectsWeekdays(
            days(monday, hours(8, 12)),
            days(monday, hours(16, 20))
        ))
    }

    @Test fun `weekdays intersection at next day`() {
        assertTrue(intersectsWeekdays(
            days(monday, hours(20, 4)),
            days(tuesday, hours(12, 20))
        ))
    }

    @Test fun `ranges on one day intersect each other`() {
        assertTrue(days(monday, hours(2, 8), hours(6, 9)).isSelfIntersecting())
    }

    @Test fun `ranges on one day do not intersect each other`() {
        assertFalse(days(monday, hours(2, 8), hours(8, 10)).isSelfIntersecting())
    }

    private fun intersects(one: OpeningWeekdays, two: OpeningWeekdays): Boolean {
        val result1 = one.intersects(two)
        val result2 = two.intersects(one)
        if (result1 != result2) fail("Intersection result was not symmetric!")
        return result1
    }

    private fun intersectsWeekdays(one: OpeningWeekdays, two: OpeningWeekdays): Boolean {
        val result1 = one.intersectsWeekdays(two)
        val result2 = two.intersectsWeekdays(one)
        if (result1 != result2) fail("intersectsWeekdays result was not symmetric!")
        return result1
    }

    private fun days(weekdays: Weekdays, vararg ranges: TimeRange) =
        OpeningWeekdays(weekdays, ranges.toMutableList())

    private fun hours(start: Int, end: Int) = TimeRange(start * 60, end * 60)
}
