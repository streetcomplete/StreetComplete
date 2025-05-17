package de.westnordost.streetcomplete.util.ktx

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

fun LocalDateTime.toEpochMilli(): Long =
    this.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
