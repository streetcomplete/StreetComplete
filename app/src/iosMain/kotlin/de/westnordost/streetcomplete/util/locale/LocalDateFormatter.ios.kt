package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toNSDateComponents
import kotlinx.datetime.toNSTimeZone
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarIdentifierGregorian
import platform.Foundation.NSDateFormatter

actual class LocalDateFormatter actual constructor(
    locale: Locale?,
    style: DateTimeFormatStyle,
) {
    private val calendar = NSCalendar(NSCalendarIdentifierGregorian).also {
        it.timeZone = TimeZone.UTC.toNSTimeZone()
    }
    private val formatter = NSDateFormatter().also {
        if (locale != null) it.locale = locale.toNSLocale()
        it.calendar = calendar
        it.timeZone = calendar.timeZone
        it.dateStyle = style.toNSDateFormatterStyle()
    }

    actual fun format(date: LocalDate): String {
        val date = calendar.dateFromComponents(date.toNSDateComponents())
            ?: return ""
        return formatter.stringFromDate(date)
    }
}
