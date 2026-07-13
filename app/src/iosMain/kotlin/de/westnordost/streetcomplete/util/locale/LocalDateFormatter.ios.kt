package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter

actual class LocalDateFormatter actual constructor(
    locale: Locale?,
    style: DateTimeFormatStyle,
) {
    private val formatter = NSDateFormatter().also {
        if (locale != null) it.locale = locale.toNSLocale()
        it.dateStyle = style.toNSDateFormatterStyle()
    }

    actual fun format(date: LocalDate): String {
        val date = NSCalendar.currentCalendar.dateFromComponents(date.toNSDateComponents())
            ?: return ""
        return formatter.stringFromDate(date)
    }
}
