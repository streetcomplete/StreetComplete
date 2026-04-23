package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

/** Convert to local date (disregarding any time passed since start of day) */
fun Instant.toLocalDate(): LocalDate = toLocalDateTime().date

/** Convert to local date time, using current system default timezone */
fun Instant.toLocalDateTime(): LocalDateTime = toLocalDateTime(TimeZone.currentSystemDefault())

/** Shortcut for Clock.System.now() */
fun systemTimeNow(): Instant = Clock.System.now()

/** Current system time in epoch milliseconds */
fun nowAsEpochMilliseconds(): Long = systemTimeNow().toEpochMilliseconds()

/** 🤪 */
fun isApril1st(): Boolean {
    val now = systemTimeNow().toLocalDate()
    return now.day == 1 && now.month == Month.APRIL
}
