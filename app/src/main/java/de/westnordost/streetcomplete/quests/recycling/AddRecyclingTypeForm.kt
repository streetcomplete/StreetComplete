package de.westnordost.streetcomplete.quests.recycling

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddRecyclingTypeForm : AImageListQuestForm<RecyclingType, RecyclingType>() {

    override val items = RecyclingType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<RecyclingType>) {
        applyAnswer(selectedItems.single())
    }
}
