@file:Suppress("NOTHING_TO_INLINE")

package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

typealias Instant = kotlinx.datetime.Instant

typealias LocalDate = kotlinx.datetime.LocalDate

typealias LocalDateTime = kotlinx.datetime.LocalDateTime

typealias LocalTime = kotlinx.datetime.LocalTime

typealias Month = kotlinx.datetime.Month

typealias ZoneId = kotlinx.datetime.TimeZone

typealias ZoneOffset = kotlinx.datetime.FixedOffsetTimeZone

fun LocalDate.toInstant(): Instant =
    this.atStartOfDayIn(ZoneId.systemDefault())

fun LocalDate.toEpochMilli(): Long = this.toInstant().toEpochMilli()

fun Instant.toLocalDate(): LocalDate =
    this.atZone(ZoneId.systemDefault()).toLocalDate()

fun isApril1st(): Boolean {
    val now = LocalDate.now()
    return now.dayOfMonth == 1 && now.month == Month.APRIL
}

inline fun Instant_Companion.ofEpochMilli(epochMilliseconds: Long): Instant =
    Instant.fromEpochMilliseconds(epochMilliseconds)

inline fun LocalDate_Companion.now(): LocalDate = Clock.System.now().toLocalDate()

inline fun LocalDate_Companion.of(year: Int, monthNumber: Int, dayOfMonth: Int): LocalDate =
    LocalDate(year, monthNumber, dayOfMonth)

fun LocalDateTime_Companion.now(): LocalDateTime =
    Clock.System.now().toLocalDateTime(ZoneId.systemDefault())

inline fun LocalDateTime_Companion.of(date: LocalDate, time: LocalTime): LocalDateTime =
    LocalDateTime(date, time)

inline fun LocalDateTime_Companion.ofInstant(instant: Instant, zone: ZoneId): LocalDateTime =
    instant.atZone(zone)

inline fun LocalTime_Companion.ofSecondOfDay(secondOfDay: Long): LocalTime =
    LocalTime.fromSecondOfDay(secondOfDay.toInt())

inline fun TimeZone_Companion.systemDefault(): ZoneId = ZoneId.currentSystemDefault()

inline fun Instant.atZone(timeZone: ZoneId): LocalDateTime = this.toLocalDateTime(timeZone)

inline fun Instant.toEpochMilli(): Long = this.toEpochMilliseconds()

inline fun LocalDate.plusDays(days: Long): LocalDate = this.plus(days, DateTimeUnit.DAY)

inline fun LocalDateTime.minusHours(hours: Long): LocalDateTime =
    this.minusInSysTZ(hours, DateTimeUnit.HOUR)

/** https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic */
fun LocalDateTime.minusInSysTZ(value: Long, unit: DateTimeUnit): LocalDateTime =
    ZoneId.systemDefault().let { tz ->
        this.toInstant(tz).minus(value, unit, tz).toLocalDateTime(tz)
    }

inline fun LocalDateTime.plusHours(hours: Long): LocalDateTime =
    this.plusInSysTZ(hours, DateTimeUnit.HOUR)

/** https://github.com/Kotlin/kotlinx-datetime#date--time-arithmetic */
fun LocalDateTime.plusInSysTZ(value: Long, unit: DateTimeUnit): LocalDateTime =
    ZoneId.systemDefault().let { tz ->
        this.toInstant(tz).plus(value, unit, tz).toLocalDateTime(tz)
    }

inline fun LocalDateTime.toLocalDate(): LocalDate = this.date

inline val FixedOffsetTimeZone_Companion.UTC: ZoneOffset get() = ZoneId.UTC

inline val LocalDate.monthValue: Int get() = this.monthNumber

private typealias Instant_Companion = kotlinx.datetime.Instant.Companion

private typealias LocalDate_Companion = kotlinx.datetime.LocalDate.Companion

private typealias LocalDateTime_Companion = kotlinx.datetime.LocalDateTime.Companion

private typealias LocalTime_Companion = kotlinx.datetime.LocalTime.Companion

private typealias TimeZone_Companion = kotlinx.datetime.TimeZone.Companion

private typealias FixedOffsetTimeZone_Companion = kotlinx.datetime.FixedOffsetTimeZone.Companion
