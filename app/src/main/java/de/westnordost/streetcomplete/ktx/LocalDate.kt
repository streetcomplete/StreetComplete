package de.westnordost.streetcomplete.ktx

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun LocalDate.toInstant() =
    this.atStartOfDay(ZoneId.systemDefault()).toInstant()

fun LocalDate.toEpochMilli() = this.toInstant().toEpochMilli()

fun Instant.toLocalDate() =
    this.atZone(ZoneId.systemDefault()).toLocalDate()
