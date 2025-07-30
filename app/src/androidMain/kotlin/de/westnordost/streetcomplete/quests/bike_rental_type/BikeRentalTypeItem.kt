package de.westnordost.streetcomplete.quests.bike_rental_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DOCKING_STATION
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DROPOFF_POINT
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.HUMAN
import de.westnordost.streetcomplete.view.image_select.Item

fun BikeRentalTypeAnswer.asItem() = Item(this, iconResId, titleResId)

private val BikeRentalTypeAnswer.titleResId: Int get() = when (this) {
    DOCKING_STATION ->    R.string.quest_bicycle_rental_type_docking_station
    DROPOFF_POINT ->      R.string.quest_bicycle_rental_type_dropoff_point
    HUMAN ->              R.string.quest_bicycle_rental_type_human
    BikeShopWithRental -> R.string.quest_bicycle_rental_type_shop_with_rental
}

private val BikeRentalTypeAnswer.iconResId: Int get() = when (this) {
    DOCKING_STATION ->     R.drawable.bicycle_rental_docking_station
    DROPOFF_POINT ->       R.drawable.bicycle_rental_dropoff_point
    HUMAN ->               R.drawable.bicycle_rental_human
    BikeShopWithRental ->  R.drawable.bicycle_rental_shop_with_rental
}
