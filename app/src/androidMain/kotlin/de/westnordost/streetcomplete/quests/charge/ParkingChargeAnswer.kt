package de.westnordost.streetcomplete.quests.charge

import de.westnordost.streetcomplete.osm.duration.DurationUnit

sealed interface ParkingChargeAnswer

data class SimpleCharge(
    // e.g. "1.50"
    val amount: Double,
    // e.g. "EUR"
    val currency: String,
    // either "day", "hour" or "minute"
    val timeUnit: DurationUnit
) : ParkingChargeAnswer
