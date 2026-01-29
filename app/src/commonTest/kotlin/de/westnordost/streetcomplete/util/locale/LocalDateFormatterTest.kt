package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateFormatterTest {
    @Test fun format() {
        val german = Locale("de")
        val date = LocalDate(1985, 11, 8)

        assertEquals(
            "08.11.85",
            LocalDateFormatter(german, DateTimeFormatStyle.Short).format(date)
        )
        assertEquals(
            "08.11.1985",
            LocalDateFormatter(german, DateTimeFormatStyle.Medium).format(date)
        )
        assertEquals(
            "8. November 1985",
            LocalDateFormatter(german, DateTimeFormatStyle.Long).format(date)
        )
        assertEquals(
            "Freitag, 8. November 1985",
            LocalDateFormatter(german, DateTimeFormatStyle.Full).format(date)
        )
    }
}
