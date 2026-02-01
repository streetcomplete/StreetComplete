package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate

/**
 * Locale-aware formatting of local dates
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used.
 * @param style which style to use
 *
 * */
expect class LocalDateFormatter(
    locale: Locale? = null,
    style: DateTimeFormatStyle = DateTimeFormatStyle.Medium,
) {
    fun format(date: LocalDate): String
}


