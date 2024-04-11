package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddTreeLeafTypeForm : AImageListQuestForm<TreeLeafType, TreeLeafType>() {

    override val items = TreeLeafType.entries.map { it.asItem() }
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<TreeLeafType>) {
        applyAnswer(selectedItems.single())
    }
}
