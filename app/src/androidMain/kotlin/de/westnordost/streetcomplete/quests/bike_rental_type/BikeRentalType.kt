package de.westnordost.streetcomplete.quests.bike_rental_type

sealed interface BikeRentalTypeAnswer

enum class BikeRentalType(val osmValue: String) : BikeRentalTypeAnswer {
    DOCKING_STATION("docking_station"),
    DROPOFF_POINT("dropoff_point"),
    HUMAN("shop"),
}

data object BikeShopWithRental : BikeRentalTypeAnswer
