package de.westnordost.streetcomplete.util

import java.text.DateFormat
import de.westnordost.streetcomplete.util.ktx.LocalDate
import de.westnordost.streetcomplete.util.ktx.LocalDateTime
import de.westnordost.streetcomplete.util.ktx.LocalTime
import de.westnordost.streetcomplete.util.ktx.ZoneId
import de.westnordost.streetcomplete.util.ktx.now
import de.westnordost.streetcomplete.util.ktx.of
import de.westnordost.streetcomplete.util.ktx.ofSecondOfDay
import de.westnordost.streetcomplete.util.ktx.systemDefault
import de.westnordost.streetcomplete.util.ktx.toEpochMilli
import kotlinx.datetime.toInstant
import java.util.Locale

fun timeOfDayToString(locale: Locale, minutes: Int): String {
    val seconds = (minutes % (24 * 60)) * 60L
    val todayAt = LocalDateTime.of(LocalDate.now(), LocalTime.ofSecondOfDay(seconds))
        .toInstant(ZoneId.systemDefault())
        .toEpochMilli()
    return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(todayAt)
}
