package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar

actual fun DayOfWeek.getDisplayName(style: DateTimeTextSymbolStyle, locale: Locale?): String {
    val calendar = NSCalendar.currentCalendar
    if (locale != null) calendar.locale = locale.nsLocale
    val symbols = when (style) {
        DateTimeTextSymbolStyle.Full -> calendar.standaloneWeekdaySymbols
        DateTimeTextSymbolStyle.Short -> calendar.shortStandaloneWeekdaySymbols
        DateTimeTextSymbolStyle.Narrow -> calendar.veryShortStandaloneWeekdaySymbols
    }
    // NSCalendar weekday symbol arrays start with Sunday, DayOfWeek starts with Monday
    return symbols[(ordinal + 1) % 7] as String
}
