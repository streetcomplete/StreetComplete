package de.westnordost.streetcomplete.quests.opening_hours.ocr

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for TimeParser - the pure Kotlin time parsing logic.
 * These tests don't require Android context since TimeParser doesn't use ML Kit.
 */
class OcrProcessorTest {

    private val parser = TimeParser

    // ==================== detectAmPm tests ====================

    @Test fun `detectAmPm returns true for AM`() {
        assertTrue(parser.detectAmPm("9:00 AM") == true)
        assertTrue(parser.detectAmPm("9AM") == true)
        assertTrue(parser.detectAmPm("am") == true)
        assertTrue(parser.detectAmPm("9 A.M.") == true)
        assertTrue(parser.detectAmPm("9 A M") == true)
    }

    @Test fun `detectAmPm returns false for PM`() {
        assertTrue(parser.detectAmPm("5:00 PM") == false)
        assertTrue(parser.detectAmPm("5PM") == false)
        assertTrue(parser.detectAmPm("pm") == false)
        assertTrue(parser.detectAmPm("5 P.M.") == false)
        assertTrue(parser.detectAmPm("5 P M") == false)
    }

    @Test fun `detectAmPm returns null when no AM or PM`() {
        assertNull(parser.detectAmPm("9:00"))
        assertNull(parser.detectAmPm("1730"))
        assertNull(parser.detectAmPm("9.30"))
        assertNull(parser.detectAmPm(""))
        assertNull(parser.detectAmPm("Open 9"))
    }

    @Test fun `detectAmPm is case insensitive`() {
        assertTrue(parser.detectAmPm("AM") == true)
        assertTrue(parser.detectAmPm("am") == true)
        assertTrue(parser.detectAmPm("Am") == true)
        assertTrue(parser.detectAmPm("aM") == true)
        assertTrue(parser.detectAmPm("PM") == false)
        assertTrue(parser.detectAmPm("pm") == false)
        assertTrue(parser.detectAmPm("Pm") == false)
    }

    // ==================== parseTimeFromText tests ====================

    @Test fun `parseTimeFromText handles HH_MM format`() {
        assertEquals("0900", parser.parseTimeFromText("9:00"))
        assertEquals("0930", parser.parseTimeFromText("9:30"))
        assertEquals("1700", parser.parseTimeFromText("17:00"))
        assertEquals("2359", parser.parseTimeFromText("23:59"))
    }

    @Test fun `parseTimeFromText handles HH dot MM format`() {
        assertEquals("0900", parser.parseTimeFromText("9.00"))
        assertEquals("0930", parser.parseTimeFromText("9.30"))
        assertEquals("1700", parser.parseTimeFromText("17.00"))
    }

    @Test fun `parseTimeFromText handles 3-4 digit format`() {
        assertEquals("0900", parser.parseTimeFromText("900"))
        assertEquals("0930", parser.parseTimeFromText("930"))
        assertEquals("0900", parser.parseTimeFromText("0900"))
        assertEquals("1730", parser.parseTimeFromText("1730"))
    }

    @Test fun `parseTimeFromText handles AM PM times`() {
        assertEquals("0900", parser.parseTimeFromText("9AM"))
        assertEquals("0900", parser.parseTimeFromText("9 AM"))
        assertEquals("2100", parser.parseTimeFromText("9PM"))
        assertEquals("2100", parser.parseTimeFromText("9 PM"))
    }

    @Test fun `parseTimeFromText handles 12AM and 12PM correctly`() {
        assertEquals("0000", parser.parseTimeFromText("12AM"))
        assertEquals("1200", parser.parseTimeFromText("12PM"))
        assertEquals("0000", parser.parseTimeFromText("12:00 AM"))
        assertEquals("1200", parser.parseTimeFromText("12:00 PM"))
    }

    @Test fun `parseTimeFromText handles empty and blank input`() {
        assertEquals("", parser.parseTimeFromText(""))
        assertEquals("", parser.parseTimeFromText("   "))
    }

    // ==================== parseTimeNumbers tests ====================

    @Test fun `parseTimeNumbers parses single digit hour`() {
        assertEquals(9 to 0, parser.parseTimeNumbers("9", isAm = true, is12HourMode = true))
        assertEquals(21 to 0, parser.parseTimeNumbers("9", isAm = false, is12HourMode = true))
    }

    @Test fun `parseTimeNumbers parses two digit hour`() {
        assertEquals(9 to 0, parser.parseTimeNumbers("09", isAm = true, is12HourMode = true))
        assertEquals(10 to 0, parser.parseTimeNumbers("10", isAm = true, is12HourMode = true))
        assertEquals(22 to 0, parser.parseTimeNumbers("10", isAm = false, is12HourMode = true))
    }

    @Test fun `parseTimeNumbers parses three digits as H MM`() {
        assertEquals(9 to 30, parser.parseTimeNumbers("930", isAm = true, is12HourMode = true))
        assertEquals(8 to 0, parser.parseTimeNumbers("800", isAm = true, is12HourMode = true))
    }

    @Test fun `parseTimeNumbers parses four digits as HH MM`() {
        assertEquals(9 to 30, parser.parseTimeNumbers("0930", isAm = true, is12HourMode = true))
        assertEquals(17 to 30, parser.parseTimeNumbers("1730", isAm = true, is12HourMode = false))
        assertEquals(8 to 0, parser.parseTimeNumbers("0800", isAm = true, is12HourMode = true))
    }

    @Test fun `parseTimeNumbers handles 12 hour mode with AM`() {
        assertEquals(9 to 0, parser.parseTimeNumbers("9", isAm = true, is12HourMode = true))
        assertEquals(0 to 0, parser.parseTimeNumbers("12", isAm = true, is12HourMode = true)) // 12 AM = midnight
    }

    @Test fun `parseTimeNumbers handles 12 hour mode with PM`() {
        assertEquals(21 to 0, parser.parseTimeNumbers("9", isAm = false, is12HourMode = true))
        assertEquals(12 to 0, parser.parseTimeNumbers("12", isAm = false, is12HourMode = true)) // 12 PM = noon
    }

    @Test fun `parseTimeNumbers handles 24 hour mode`() {
        assertEquals(9 to 0, parser.parseTimeNumbers("9", isAm = true, is12HourMode = false))
        assertEquals(17 to 30, parser.parseTimeNumbers("1730", isAm = false, is12HourMode = false))
        assertEquals(0 to 0, parser.parseTimeNumbers("0000", isAm = true, is12HourMode = false))
    }

    @Test fun `parseTimeNumbers returns null for invalid input`() {
        assertNull(parser.parseTimeNumbers("", isAm = true, is12HourMode = true))
        assertNull(parser.parseTimeNumbers("abc", isAm = true, is12HourMode = true))
    }

    @Test fun `parseTimeNumbers returns null for invalid minutes`() {
        // Minutes > 59 should be invalid
        assertNull(parser.parseTimeNumbers("0965", isAm = true, is12HourMode = true)) // 65 minutes
    }

    @Test fun `parseTimeNumbers handles typical business hours`() {
        // Common open times
        assertEquals(9 to 0, parser.parseTimeNumbers("0900", isAm = true, is12HourMode = true))
        assertEquals(8 to 30, parser.parseTimeNumbers("0830", isAm = true, is12HourMode = true))

        // Common close times
        assertEquals(17 to 0, parser.parseTimeNumbers("0500", isAm = false, is12HourMode = true)) // 5 PM
        assertEquals(18 to 30, parser.parseTimeNumbers("0630", isAm = false, is12HourMode = true)) // 6:30 PM
        assertEquals(21 to 0, parser.parseTimeNumbers("0900", isAm = false, is12HourMode = true)) // 9 PM
    }
}
