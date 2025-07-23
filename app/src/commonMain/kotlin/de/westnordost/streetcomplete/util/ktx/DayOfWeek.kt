package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek

/** Weekday name in current locale, e.g. "Monday" */
val DayOfWeek.displayName: String get() = getDisplayName(Locale.current)

/** Short weekday name in current locale, e.g. "Mon" */
val DayOfWeek.shortDisplayName: String get() = getShortDisplayName(Locale.current)

/** Abbreviated weekday name in current locale, e.g. "M" */
val DayOfWeek.narrowDisplayName: String get() = getNarrowDisplayName(Locale.current)

/** Weekday name in given [locale], e.g. "Monday" */
expect fun DayOfWeek.getDisplayName(locale: Locale): String

/** Short weekday name in given [locale], e.g. "Mon" */
expect fun DayOfWeek.getShortDisplayName(locale: Locale): String

/** Abbreviated weekday name in given [locale], e.g. "M" */
expect fun DayOfWeek.getNarrowDisplayName(locale: Locale): String
