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

    @Test fun zh_CN() {
        assertEquals(
            DateFormatElements(order = listOf(Year, Month, Day), separator = "/"),
            DateFormatElements.of(Locale("zh-CN"))
        )
    }

    @Test fun ja() {
        assertEquals(
            DateFormatElements(order = listOf(Year, Month, Day), separator = "/"),
            DateFormatElements.of(Locale("ja"))
        )
    }

    @Test fun ko() {
        assertEquals(
            DateFormatElements(order = listOf(Year, Month, Day), separator = "."),
            DateFormatElements.of(Locale("ko"))
        )
    }

    @Test fun hu() {
        assertEquals(
            DateFormatElements(order = listOf(Year, Month, Day), separator = "."),
            DateFormatElements.of(Locale("hu"))
        )
    }
}
