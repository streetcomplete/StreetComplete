package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone

/**
 * Locale-aware formatting of local date times
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used.
 * @param timeZone in which time zone the date time is assumed
 * @param dateStyle which date format to use
 * @param timeStyle which time format to use
 *
 * */
expect class LocalDateTimeFormatter(
    locale: Locale? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    dateStyle: DateTimeFormatStyle = DateTimeFormatStyle.Medium,
    timeStyle: DateTimeFormatStyle = dateStyle,
) {
    fun format(dateTime: LocalDateTime): String
}
