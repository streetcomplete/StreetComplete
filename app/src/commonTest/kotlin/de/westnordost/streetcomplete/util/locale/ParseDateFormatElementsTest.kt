package de.westnordost.streetcomplete.util.locale

import de.westnordost.streetcomplete.util.locale.DateComponent.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

// 
class ParseDateFormatElementsTest {
    @Test fun fourDigitYear() {
        assertEquals(
            DateFormatElements(order = listOf(Month, Day, Year), separator = "/"),
            parseDateFormatElements(locale = null) { "03/05/2026" }
        )
    }
    
    @Test fun twoDigitYear() {
        assertEquals(
            DateFormatElements(order = listOf(Month, Day, Year), separator = "/"),
            parseDateFormatElements(locale = null) { "03/05/26" }
        )
    }

    @Test fun hyphenSeparator() {
        assertEquals(
            DateFormatElements(order = listOf(Day, Month, Year), separator = "-"),
            parseDateFormatElements(locale = null) { "5-3-2026" }
        )
    }   

    @Test fun missingComponentReturnsNull() {
        assertNull(parseDateFormatElements(locale = null) { "2026/03" })
    }

    @Test fun noSeparatorDefaultsToSlash() {
        assertEquals(
            DateFormatElements(order = listOf(Year, Month, Day), separator = "/"),
            parseDateFormatElements(locale = null) { "20260305" }
        )
    }

    @Test fun spacePaddedSeparatorIsTrimmed() {
        assertEquals(
            DateFormatElements(order = listOf(Day, Month, Year), separator = "-"),
            parseDateFormatElements(locale = null) { "5 - 3 - 2026" }
        )
    }
}
