package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class LocalTimeFormatterTest {
    @Test fun format() {
        val german = Locale("de")
        val time = LocalTime(12, 23, 40)

        assertEquals(
            "12:23",
            LocalTimeFormatter(german, style = DateTimeFormatStyle.Short).format(time)
        )
        assertEquals(
            "12:23:40",
            LocalTimeFormatter(german, style = DateTimeFormatStyle.Medium).format(time)
        )
        assertEquals(
            "12:23:40 MEZ",
            LocalTimeFormatter(german, timeZone = TimeZone.of("CET"), style = DateTimeFormatStyle.Long).format(time)
        )
        assertEquals(
            "12:23:40 Mitteleuropäische Zeit",
            LocalTimeFormatter(german, timeZone = TimeZone.of("CET"), style = DateTimeFormatStyle.Full).format(time)
        )
    }
}
