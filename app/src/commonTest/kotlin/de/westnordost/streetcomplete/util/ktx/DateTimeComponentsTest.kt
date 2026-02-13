package de.westnordost.streetcomplete.util.ktx

import de.westnordost.streetcomplete.util.ktx.RFC_2822_STRICT
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.format.DateTimeComponents.Formats
import kotlin.test.Test
import kotlin.test.assertEquals

class DateTimeComponentsTest {
    @Test fun `parse minimum`() {
        val instant = Formats.RFC_2822_STRICT.parse("01 Feb 2026 12:14 -0130")
        assertEquals(2026, instant.year)
        assertEquals(Month.FEBRUARY, instant.month)
        assertEquals(1, instant.dayOfMonth)
        assertEquals(12, instant.hour)
        assertEquals(14, instant.minute)
        assertEquals(0, instant.second)
        assertEquals(true, instant.offsetIsNegative)
        assertEquals(1, instant.offsetHours)
        assertEquals(30, instant.offsetMinutesOfHour)
    }

    @Test fun parse() {
        val instant = Formats.RFC_2822_STRICT.parse("Sun, 01 Feb 2026 12:14:16 -0130")
        assertEquals(2026, instant.year)
        assertEquals(Month.FEBRUARY, instant.month)
        assertEquals(1, instant.dayOfMonth)
        assertEquals(DayOfWeek.SUNDAY, instant.dayOfWeek)
        assertEquals(12, instant.hour)
        assertEquals(14, instant.minute)
        assertEquals(16, instant.second)
        assertEquals(true, instant.offsetIsNegative)
        assertEquals(1, instant.offsetHours)
        assertEquals(30, instant.offsetMinutesOfHour)
    }
}
