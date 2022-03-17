package de.westnordost.streetcomplete.util.ktx

import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId

fun LocalDate.toInstant(): Instant =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant()

fun LocalDate.toEpochMilli(): Long = this.toInstant().toEpochMilli()

fun Instant.toLocalDate(): LocalDate =
    this.atZone(ZoneId.systemDefault()).toLocalDate()

fun isApril1st(): Boolean {
    val now = LocalDate.now()
    return now.dayOfMonth == 1 && now.month == Month.APRIL
}
