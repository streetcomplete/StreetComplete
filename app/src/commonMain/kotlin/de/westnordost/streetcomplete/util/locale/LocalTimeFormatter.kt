package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * Locale-aware formatting of local times
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used.
 * @param style which style to use
 *
 * */
expect class LocalTimeFormatter(
    locale: Locale? = null,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    style: DateTimeFormatStyle = DateTimeFormatStyle.Medium,
) {
    fun format(time: LocalTime): String
}
