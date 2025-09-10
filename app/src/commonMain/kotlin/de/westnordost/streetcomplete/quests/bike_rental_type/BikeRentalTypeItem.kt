package de.westnordost.streetcomplete.quests.bike_rental_type

import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.*
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalTypeAnswer.BikeShopWithRental
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bicycle_rental_docking_station
import de.westnordost.streetcomplete.resources.bicycle_rental_dropoff_point
import de.westnordost.streetcomplete.resources.bicycle_rental_human
import de.westnordost.streetcomplete.resources.bicycle_rental_shop_with_rental
import de.westnordost.streetcomplete.resources.quest_bicycle_rental_type_docking_station
import de.westnordost.streetcomplete.resources.quest_bicycle_rental_type_dropoff_point
import de.westnordost.streetcomplete.resources.quest_bicycle_rental_type_human
import de.westnordost.streetcomplete.resources.quest_bicycle_rental_type_shop_with_rental
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BikeRentalTypeAnswer.title: StringResource get() = when (this) {
    DOCKING_STATION ->    Res.string.quest_bicycle_rental_type_docking_station
    DROPOFF_POINT ->      Res.string.quest_bicycle_rental_type_dropoff_point
    HUMAN ->              Res.string.quest_bicycle_rental_type_human
    BikeShopWithRental -> Res.string.quest_bicycle_rental_type_shop_with_rental
}

val BikeRentalTypeAnswer.icon: DrawableResource get() = when (this) {
    DOCKING_STATION ->     Res.drawable.bicycle_rental_docking_station
    DROPOFF_POINT ->       Res.drawable.bicycle_rental_dropoff_point
    HUMAN ->               Res.drawable.bicycle_rental_human
    BikeShopWithRental ->  Res.drawable.bicycle_rental_shop_with_rental
}
