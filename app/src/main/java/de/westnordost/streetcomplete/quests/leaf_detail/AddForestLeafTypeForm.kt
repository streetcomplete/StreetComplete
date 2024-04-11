package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddForestLeafTypeForm : AImageListQuestForm<ForestLeafType, ForestLeafType>() {

    override val items = ForestLeafType.entries.map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ForestLeafType>) {
        applyAnswer(selectedItems.single())
    }
}
