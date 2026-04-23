package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalTime
import kotlinx.datetime.toJavaZoneId
import java.time.format.DateTimeFormatter

actual class LocalTimeFormatter actual constructor(
    locale: Locale?,
    timeZone: TimeZone,
    style: DateTimeFormatStyle,
) {
    private val formatter = DateTimeFormatter
        .ofLocalizedTime(style.toFormatStyle())
        .let { if (locale != null) it.withLocale(locale.platformLocale) else it }
        .withZone(timeZone.toJavaZoneId())

    actual fun format(time: LocalTime): String =
        time.toJavaLocalTime().format(formatter)
}
