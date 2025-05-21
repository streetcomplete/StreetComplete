package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Convert to `Instant`, using the current system default timezone */
fun LocalDate.toInstant(): Instant = atStartOfDayIn(TimeZone.currentSystemDefault())

/**  Return this in epoch milliseconds, using the current system default timezone */
fun LocalDate.toEpochMilli(): Long = toInstant().toEpochMilliseconds()

/** Convert to `Instant`, using the current system default timezone */
fun LocalDateTime.toInstant(): Instant = toInstant(TimeZone.currentSystemDefault())

/** Return this in epoch milliseconds, using the current system default timezone */
fun LocalDateTime.toEpochMilli(): Long = toInstant().toEpochMilliseconds()

/** Convert to local date (disregarding any time passed since start of day) */
fun Instant.toLocalDate(): LocalDate = toLocalDateTime().date

/** Convert to local date time, using current system default timezone */
fun Instant.toLocalDateTime(): LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())

/** 🤪 */
fun isApril1st(): Boolean {
    val now = systemTimeNow().toLocalDate()
    return now.dayOfMonth == 1 && now.month == Month.APRIL
}

/** Current system time in epoch milliseconds */
fun nowAsEpochMilliseconds(): Long = systemTimeNow().toEpochMilliseconds()

/** Shortcut for Clock.System.now() */
fun systemTimeNow(): Instant = Clock.System.now()

/** Current `LocalDateTime`, using the current system default timezone */
fun LocalDateTime.Companion.now(): LocalDateTime =
    systemTimeNow().toLocalDateTime(TimeZone.currentSystemDefault())

/**
 * Subtract some time from this, using current system default timezone.
 *
 * See also https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic
 * */
fun LocalDateTime.minusInSystemTimeZone(value: Long, unit: DateTimeUnit): LocalDateTime {
    val tz = TimeZone.currentSystemDefault()
    return toInstant(tz).minus(value, unit, tz).toLocalDateTime(tz)
}

/**
 * Add some time to this, using current system default timezone.
 *
 * See also https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic
 * */
fun LocalDateTime.plusInSystemTimeZone(value: Long, unit: DateTimeUnit): LocalDateTime {
    val tz = TimeZone.currentSystemDefault()
    return toInstant(tz).plus(value, unit, tz).toLocalDateTime(tz)
}
