package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/** Convert to `Instant`, using the current system default timezone */
fun LocalDate.toInstant(): Instant = atStartOfDayIn(TimeZone.currentSystemDefault())

/**  Return this in epoch milliseconds, using the current system default timezone */
fun LocalDate.toEpochMilli(): Long = toInstant().toEpochMilliseconds()

/** Current `LocalDate`, using the current system default timezone */
fun LocalDate.Companion.now(): LocalDate =
    systemTimeNow().toLocalDate()
