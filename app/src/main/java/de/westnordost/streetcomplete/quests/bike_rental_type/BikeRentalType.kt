package de.westnordost.streetcomplete.quests.bike_rental_type

enum class BikeRentalType(val osmValue: String) {
    DOCKING_STATION("docking_station"),
    DROPOFF_POINT("dropoff_point"),
    KEY_DISPENSING_MACHINE("key_dispensing_machine"),
    SHOP("shop"),
}
