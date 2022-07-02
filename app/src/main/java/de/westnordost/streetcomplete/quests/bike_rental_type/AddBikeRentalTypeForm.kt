package de.westnordost.streetcomplete.quests.bike_rental_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DOCKING_STATION
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.DROPOFF_POINT
import de.westnordost.streetcomplete.quests.bike_rental_type.BikeRentalType.HUMAN
import de.westnordost.streetcomplete.view.image_select.Item

class AddBikeRentalTypeForm : AImageListQuestForm<BikeRentalTypeAnswer, BikeRentalTypeAnswer>() {

    override val items: List<Item<BikeRentalTypeAnswer>> = listOf(
        Item(DOCKING_STATION, R.drawable.bicycle_rental_docking_station, R.string.quest_bicycle_rental_type_docking_station),
        Item(DROPOFF_POINT, R.drawable.bicycle_rental_dropoff_point, R.string.quest_bicycle_rental_type_dropoff_point),
        Item(HUMAN, R.drawable.bicycle_rental_human, R.string.quest_bicycle_rental_type_human),
        Item(BikeShopWithRental, R.drawable.bicycle_rental_shop_with_rental, R.string.quest_bicycle_rental_type_shop_with_rental),
    )

    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BikeRentalTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
