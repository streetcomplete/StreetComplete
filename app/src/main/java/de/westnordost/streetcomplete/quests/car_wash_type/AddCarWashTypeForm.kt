package de.westnordost.streetcomplete.quests.car_wash_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddCarWashTypeForm : AImageListQuestForm<CarWashType, List<CarWashType>>() {

    override val items = CarWashType.values().map { it.asItem() }
    override val itemsPerRow = 3
    override val maxSelectableItems = -1

    override fun onClickOk(selectedItems: List<CarWashType>) {
        applyAnswer(selectedItems)
    }
}
