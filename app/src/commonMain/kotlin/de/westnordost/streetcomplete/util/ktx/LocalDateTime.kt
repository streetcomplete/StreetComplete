package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/** Convert to `Instant`, using the current system default timezone */
fun LocalDateTime.toInstant(): Instant = toInstant(TimeZone.currentSystemDefault())

/** Return this in epoch milliseconds, using the current system default timezone */
fun LocalDateTime.toEpochMilli(): Long = toInstant().toEpochMilliseconds()

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
