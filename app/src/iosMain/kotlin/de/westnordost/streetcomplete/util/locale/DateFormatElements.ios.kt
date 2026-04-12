package de.westnordost.streetcomplete.util.locale

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale

// Honour the iOS Language & Region → Date Format override.
// NSLocale(localeIdentifier:) does NOT reflect this setting;
// only NSLocale.currentLocale does.
actual fun systemDefaultDateFormatElements(): DateFormatElements? {
    val formatter = NSDateFormatter().also {
        it.locale = NSLocale.currentLocale
        it.dateStyle = DateTimeFormatStyle.Short.toNSDateFormatterStyle()
        it.timeStyle = 0u
    }
    val probeDate = LocalDate(2026, 3, 5)
    val nsDate = NSCalendar.currentCalendar.dateFromComponents(probeDate.toNSDateComponents())
        ?: return null
    val formatted = formatter.stringFromDate(nsDate)
    return parseDateFormatElements(formatted, locale = null)
}
