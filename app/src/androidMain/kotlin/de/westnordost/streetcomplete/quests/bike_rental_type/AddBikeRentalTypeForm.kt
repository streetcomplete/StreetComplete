package de.westnordost.streetcomplete.quests.bike_rental_type

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddBikeRentalTypeForm : AImageListQuestComposeForm<BikeRentalTypeAnswer, BikeRentalTypeAnswer>() {

    override val items = BikeRentalType.entries.map { it.asItem() } + BikeShopWithRental.asItem()
    override val itemsPerRow = 2
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BikeRentalTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
