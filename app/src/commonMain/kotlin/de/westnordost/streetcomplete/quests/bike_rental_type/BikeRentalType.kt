package de.westnordost.streetcomplete.quests.bike_rental_type

import kotlinx.serialization.Serializable

@Serializable
enum class BikeRentalTypeAnswer {
    DOCKING_STATION,
    DROPOFF_POINT,
    HUMAN,
    BIKE_SHOP_WITH_RENTAL
}
