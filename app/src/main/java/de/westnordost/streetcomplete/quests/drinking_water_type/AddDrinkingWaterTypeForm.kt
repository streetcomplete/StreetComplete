package de.westnordost.streetcomplete.quests.drinking_water_type

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddDrinkingWaterTypeForm : AImageListQuestComposeForm<DrinkingWaterType, DrinkingWaterType>() {

    override val items = DrinkingWaterType.entries.map { it.asItem() }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<DrinkingWaterType>) {
        applyAnswer(selectedItems.single())
    }
}
