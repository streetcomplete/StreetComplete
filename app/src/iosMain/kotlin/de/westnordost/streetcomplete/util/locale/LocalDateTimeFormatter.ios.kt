package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDateComponents
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter

actual class LocalDateTimeFormatter actual constructor(
    locale: Locale?,
    timeZone: TimeZone,
    dateStyle: DateTimeFormatStyle,
    timeStyle: DateTimeFormatStyle,
) {
    private val formatter = NSDateFormatter().also {
        if (locale != null) it.locale = locale.platformLocale
        it.dateStyle = dateStyle.toNSDateFormatterStyle()
        it.timeStyle = timeStyle.toNSDateFormatterStyle()
        it.timeZone = timeZone.toNSTimeZone()
    }

    actual fun format(dateTime: LocalDateTime): String {
        val date = NSCalendar.currentCalendar.dateFromComponents(dateTime.toNSDateComponents())
            ?: return ""
        return formatter.stringFromDate(date)
    }
}
