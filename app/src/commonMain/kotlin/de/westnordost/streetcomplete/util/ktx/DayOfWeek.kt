package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek

/** Weekday name in given [locale], e.g. "Monday" */
expect fun DayOfWeek.getDisplayName(locale: Locale? = null): String

/** Short weekday name in given [locale], e.g. "Mon" */
expect fun DayOfWeek.getShortDisplayName(locale: Locale? = null): String

/** Abbreviated weekday name in given [locale], e.g. "M" */
expect fun DayOfWeek.getNarrowDisplayName(locale: Locale? = null): String
