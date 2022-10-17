@file:Suppress("NOTHING_TO_INLINE")

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

fun LocalDate.toInstant(): Instant =
    this.atStartOfDayIn(TimeZone.currentSystemDefault())

fun LocalDate.toEpochMilli(): Long = this.toInstant().toEpochMilliseconds()

fun Instant.toLocalDate(): LocalDate =
    this.toLocalDateTime(TimeZone.currentSystemDefault()).date

fun isApril1st(): Boolean {
    val now = Clock.System.now().toLocalDate()
    return now.dayOfMonth == 1 && now.month == Month.APRIL
}

fun LocalDateTime.Companion.now(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

/** https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic */
fun LocalDateTime.minusInSysTZ(value: Long, unit: DateTimeUnit): LocalDateTime =
    TimeZone.currentSystemDefault().let { tz ->
        this.toInstant(tz).minus(value, unit, tz).toLocalDateTime(tz)
    }

/** https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic */
fun LocalDateTime.plusInSysTZ(value: Long, unit: DateTimeUnit): LocalDateTime =
    TimeZone.currentSystemDefault().let { tz ->
        this.toInstant(tz).plus(value, unit, tz).toLocalDateTime(tz)
    }
