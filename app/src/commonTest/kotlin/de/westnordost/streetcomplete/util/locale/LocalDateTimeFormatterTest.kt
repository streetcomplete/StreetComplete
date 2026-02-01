package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalDateTimeFormatterTest {
    @Test fun format() {
        val german = Locale("de")
        val dateTime = LocalDateTime(LocalDate(1985, 11, 8), LocalTime(18, 30, 24))

        assertEquals(
            "08.11.85, 18:30",
            LocalDateTimeFormatter(german, dateStyle = DateTimeFormatStyle.Short).format(dateTime)
        )
        assertEquals(
            "08.11.1985, 18:30:24",
            LocalDateTimeFormatter(german, dateStyle = DateTimeFormatStyle.Medium).format(dateTime)
        )
        assertEquals(
            "8. November 1985, 18:30:24 MEZ",
            LocalDateTimeFormatter(german, timeZone = TimeZone.of("CET"), dateStyle = DateTimeFormatStyle.Long).format(dateTime)
        )
        assertEquals(
            "Freitag, 8. November 1985, 18:30:24 Mitteleuropäische Zeit",
            LocalDateTimeFormatter(german, timeZone = TimeZone.of("CET"), dateStyle = DateTimeFormatStyle.Full).format(dateTime)
        )
    }
}
