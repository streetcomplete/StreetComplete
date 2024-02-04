package de.westnordost.streetcomplete.quests.crossing_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddCrossingTypeForm : AImageListQuestForm<CrossingType, CrossingType>() {

    override val items = CrossingType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<CrossingType>) {
        applyAnswer(selectedItems.single())
    }
}
