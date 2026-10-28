package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter

actual class LocalDateTimeFormatter actual constructor(
    locale: Locale?,
    timeZone: TimeZone,
    dateStyle: DateTimeFormatStyle,
    timeStyle: DateTimeFormatStyle,
) {
    private val formatter = DateTimeFormatter
        .ofLocalizedDateTime(dateStyle.toFormatStyle(), timeStyle.toFormatStyle())
        .let { if (locale != null) it.withLocale(locale.platformLocale) else it }
        .withZone(timeZone.toJavaZoneId())

    actual fun format(dateTime: LocalDateTime): String =
        dateTime.toJavaLocalDateTime().format(formatter)
}
