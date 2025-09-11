package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddCarWashTypeForm : AImageListQuestComposeForm<CarWashType, List<CarWashType>>() {

    override val items = CarWashType.entries.map { it.asItem() }
    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<CarWashType>) {
        applyAnswer(selectedItems)
    }
}
