package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn

/** Convert to `Instant`, using the current system default timezone */
fun LocalDate.toInstant(): Instant = atStartOfDayIn(TimeZone.currentSystemDefault())

/**  Return this in epoch milliseconds, using the current system default timezone */
fun LocalDate.toEpochMilli(): Long = toInstant().toEpochMilliseconds()
