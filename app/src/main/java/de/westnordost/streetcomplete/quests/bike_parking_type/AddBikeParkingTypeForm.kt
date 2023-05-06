package de.westnordost.streetcomplete.quests.bike_parking_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBikeParkingTypeForm : AImageListQuestForm<BikeParkingType, BikeParkingType>() {

    override val items = BikeParkingType.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BikeParkingType>) {
        applyAnswer(selectedItems.single())
    }
}
