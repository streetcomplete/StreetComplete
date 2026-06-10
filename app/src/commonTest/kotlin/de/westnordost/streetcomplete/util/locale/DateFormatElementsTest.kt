package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateComponent.*
import kotlin.test.Test
import kotlin.test.assertEquals

class DateFormatElementsTest {
    @Test fun en_US() {
        assertEquals(
            DateFormatElements(order = listOf(Month, Day, Year), separator = "/"),
            DateFormatElements.of(Locale("en-US"))
        )
    }

    @Test fun en_GB() {
        assertEquals(
            DateFormatElements(order = listOf(Day, Month, Year), separator = "/"),
            DateFormatElements.of(Locale("en-GB"))
        )
    }

    @Test fun de() {
        assertEquals(
            DateFormatElements(order = listOf(Day, Month, Year), separator = "."),
            DateFormatElements.of(Locale("de"))
        )
    }

    @Test fun th_TH() {
        assertEquals(
            DateFormatElements(listOf(Day, Month, Year), "/"),
            DateFormatElements.of(Locale("th-TH"))
        )
    }

    @Test fun fr_CA() {
        assertEquals(
            DateFormatElements(listOf(Year, Month, Day), "-"),
            DateFormatElements.of(Locale("fr-CA"))
        )
    }

    @Test fun sk() {
        assertEquals(
            DateFormatElements(listOf(Day, Month, Year), ". "),
            DateFormatElements.of(Locale("sk"))
        )
    }

    @Test fun bg() {
        assertEquals(
            DateFormatElements(listOf(Day, Month, Year), ".", before = "", after = " г."),
            DateFormatElements.of(Locale("bg"))
        )
    }
}
