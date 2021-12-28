package de.westnordost.streetcomplete.util

import java.text.DateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Locale

fun timeOfDayToString(locale: Locale, minutes: Int): String {
    val seconds = (minutes % (24*60)) * 60L
    val todayAt = LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(seconds))
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(todayAt)
}
