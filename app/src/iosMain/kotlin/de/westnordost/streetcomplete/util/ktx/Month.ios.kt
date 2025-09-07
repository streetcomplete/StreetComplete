package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Month
import platform.Foundation.NSCalendar

actual fun Month.getDisplayName(locale: Locale?): String =
    NSCalendar.currentCalendar
        .also { if (locale != null) it.locale = locale.platformLocale }
        .standaloneMonthSymbols[ordinal] as String

actual fun Month.getShortDisplayName(locale: Locale?): String =
    NSCalendar.currentCalendar
        .also { if (locale != null) it.locale = locale.platformLocale }
        .shortStandaloneMonthSymbols[ordinal] as String

actual fun Month.getNarrowDisplayName(locale: Locale?): String =
    NSCalendar.currentCalendar
        .also { if (locale != null) it.locale = locale.platformLocale }
        .veryShortStandaloneMonthSymbols[ordinal] as String
