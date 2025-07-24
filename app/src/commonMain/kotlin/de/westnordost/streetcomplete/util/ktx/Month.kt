package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Month

/** Month name in given [locale], e.g. "January" */
expect fun Month.getDisplayName(locale: Locale? = null): String

/** Short month name in given [locale], e.g. "Jan" */
expect fun Month.getShortDisplayName(locale: Locale? = null): String

/** Abbreviated month name in given [locale], e.g. "J" */
expect fun Month.getNarrowDisplayName(locale: Locale? = null): String
