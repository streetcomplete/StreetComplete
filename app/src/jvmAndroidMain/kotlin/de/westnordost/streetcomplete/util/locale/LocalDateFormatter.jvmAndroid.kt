package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter

actual class LocalDateFormatter actual constructor(
    locale: Locale?,
    style: DateTimeFormatStyle,
) {
    private val formatter = DateTimeFormatter
        .ofLocalizedDate(style.toFormatStyle())
        .let { if (locale != null) it.withLocale(locale.platformLocale) else it }

    actual fun format(date: LocalDate): String =
        date.toJavaLocalDate().format(formatter)
}
