package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Month

/** Month name in current locale, e.g. "January" */
val Month.displayName: String get() = getDisplayName(Locale.current)

/** Short month name in current locale, e.g. "Jan" */
val Month.shortDisplayName: String get() = getShortDisplayName(Locale.current)

/** Abbreviated month name in current locale, e.g. "J" */
val Month.narrowDisplayName: String get() = getNarrowDisplayName(Locale.current)

/** Month name in given [locale], e.g. "January" */
expect fun Month.getDisplayName(locale: Locale): String

/** Short month name in given [locale], e.g. "Jan" */
expect fun Month.getShortDisplayName(locale: Locale): String

/** Abbreviated month name in given [locale], e.g. "J" */
expect fun Month.getNarrowDisplayName(locale: Locale): String
