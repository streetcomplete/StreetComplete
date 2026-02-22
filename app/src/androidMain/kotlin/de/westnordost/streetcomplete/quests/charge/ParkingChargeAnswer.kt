package de.westnordost.streetcomplete.quests.charge

sealed interface ParkingChargeAnswer

data class SimpleCharge(
    // e.g. "1.50"
    val amount: String,
    // e.g. "EUR"
    val currency: String,
    // either "day", "hour" or "minute"
    val timeUnit: String
) : ParkingChargeAnswer

data class ItVaries(
    val conditional: String
) : ParkingChargeAnswer
