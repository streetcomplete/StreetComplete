package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar

actual fun DayOfWeek.getDisplayName(style: DateTimeTextSymbolStyle, locale: Locale?): String {
    val calendar = NSCalendar.currentCalendar
    if (locale != null) calendar.locale = locale.platformLocale
    val symbols = when (style) {
        DateTimeTextSymbolStyle.Full -> calendar.standaloneWeekdaySymbols
        DateTimeTextSymbolStyle.Short -> calendar.shortStandaloneWeekdaySymbols
        DateTimeTextSymbolStyle.Narrow -> calendar.veryShortStandaloneWeekdaySymbols
    }
    return symbols[ordinal] as String
}
