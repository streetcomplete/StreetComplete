package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone

/**
 * Locale-aware formatting of local times
 *
 * @param locale Locale to use. If [locale] is `null`, the default locale (for formatting) will be
 *   used. Note that for example iOS allows the user to select whether the time should be formatted
 *   as a 24-hour or 12-hour clock. This setting is only respected when the default locale is used,
 *   once a locale is specified explicitly, always the default formatting rules for that locale are
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
