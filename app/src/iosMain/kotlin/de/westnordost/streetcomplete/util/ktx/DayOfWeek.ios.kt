package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import platform.Foundation.NSCalendar

actual fun DayOfWeek.getDisplayName(locale: Locale): String =
    NSCalendar.currentCalendar
        .also { it.locale = locale.platformLocale }
        .standaloneWeekdaySymbols[ordinal] as String

actual fun DayOfWeek.getShortDisplayName(locale: Locale): String =
    NSCalendar.currentCalendar
        .also { it.locale = locale.platformLocale }
        .shortStandaloneWeekdaySymbols[ordinal] as String

actual fun DayOfWeek.getNarrowDisplayName(locale: Locale): String =
    NSCalendar.currentCalendar
        .also { it.locale = locale.platformLocale }
        .veryShortStandaloneWeekdaySymbols[ordinal] as String
