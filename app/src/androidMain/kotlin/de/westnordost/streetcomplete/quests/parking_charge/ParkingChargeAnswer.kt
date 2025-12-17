package de.westnordost.streetcomplete.quests.parking_charge

sealed interface ParkingChargeAnswer

data class SimpleCharge(
    val amount: String,  // z.B. "1.50"
    val currency: String,  // z.B. "EUR"
    val timeUnit: String  // z.B. "hour", "30 minutes"
) : ParkingChargeAnswer

data class ItVaries(
    val description: String
) : ParkingChargeAnswer
