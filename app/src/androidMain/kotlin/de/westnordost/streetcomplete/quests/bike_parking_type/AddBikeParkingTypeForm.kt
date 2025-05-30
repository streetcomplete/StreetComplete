package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddBikeParkingTypeForm : AImageListQuestComposeForm<BikeParkingType, BikeParkingType>() {

    override val items = BikeParkingType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BikeParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
