package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDateComponents
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle

actual class LocalTimeFormatter actual constructor(
    locale: Locale?,
    timeZone: TimeZone,
    style: DateTimeFormatStyle,
) {
    private val formatter = NSDateFormatter().also {
        if (locale != null) it.locale = locale.platformLocale
        it.dateStyle = NSDateFormatterNoStyle
        it.timeStyle = style.toNSDateFormatterStyle()
        it.timeZone = timeZone.toNSTimeZone()
    }

    actual fun format(time: LocalTime): String {
        val dateTime = LocalDateTime(LocalDate(2000, 1, 1), time)
        val date = NSCalendar.currentCalendar.dateFromComponents(dateTime.toNSDateComponents())
            ?: return ""
        return formatter.stringFromDate(date)
    }
}
