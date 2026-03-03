package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.Month
import platform.Foundation.NSCalendar

actual fun Month.getDisplayName(
    style: DateTimeTextSymbolStyle,
    locale: Locale?
): String {
    val calendar = NSCalendar.currentCalendar
    if (locale != null) calendar.locale = locale.platformLocale
    val symbols = when (style) {
        DateTimeTextSymbolStyle.Full -> calendar.standaloneMonthSymbols
        DateTimeTextSymbolStyle.Short -> calendar.shortStandaloneMonthSymbols
        DateTimeTextSymbolStyle.Narrow -> calendar.veryShortStandaloneMonthSymbols
    }
    return symbols[ordinal] as String
}
