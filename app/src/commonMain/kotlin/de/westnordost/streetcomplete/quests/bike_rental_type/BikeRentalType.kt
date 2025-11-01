package de.westnordost.streetcomplete.quests.bike_rental_type

import kotlinx.serialization.Serializable

@Serializable
sealed interface BikeRentalTypeAnswer {
    data object BikeShopWithRental : BikeRentalTypeAnswer
}

enum class BikeRentalType(val osmValue: String) : BikeRentalTypeAnswer {
    DOCKING_STATION("docking_station"),
    DROPOFF_POINT("dropoff_point"),
    HUMAN("shop"),
}
