package de.westnordost.streetcomplete.util

import de.westnordost.streetcomplete.util.ktx.systemTimeNow
import de.westnordost.streetcomplete.util.ktx.toLocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDateTime
import java.text.DateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

fun timeOfDayToString(locale: Locale, minutes: Int): String {
    val seconds = (minutes % (24 * 60)) * 60L
    val todayAt = LocalDateTime(systemTimeNow().toLocalDate(), LocalTime.fromSecondOfDay(seconds.toInt()))
        .toInstant(TimeZone.currentSystemDefault())
        .toEpochMilliseconds()
    return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(todayAt)
}

fun dateTimeToString(locale: Locale, dateTime: LocalDateTime): String {
    val formatter = DateTimeFormatter
        .ofLocalizedDateTime(FormatStyle.SHORT, FormatStyle.SHORT)
        .withLocale(locale)

    return dateTime.toJavaLocalDateTime().format(formatter)
}
