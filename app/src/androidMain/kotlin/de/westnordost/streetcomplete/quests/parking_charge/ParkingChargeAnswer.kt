package de.westnordost.streetcomplete.quests.parking_charge

sealed interface ParkingChargeAnswer

data class SimpleCharge(
    // e.g. "1.50"
    val amount: String,
    // e.g. "EUR"
    val currency: String,
    // e.g. "hour", "30 minutes"
    val timeUnit: String
) : ParkingChargeAnswer

data class ItVaries(
    val conditional: String
) : ParkingChargeAnswer
