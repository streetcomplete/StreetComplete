package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.leaf_detail.ForestLeafType.BROADLEAVED
import de.westnordost.streetcomplete.quests.leaf_detail.ForestLeafType.MIXED
import de.westnordost.streetcomplete.quests.leaf_detail.ForestLeafType.NEEDLELEAVED
import de.westnordost.streetcomplete.view.image_select.Item

class AddForestLeafTypeForm : AImageListQuestForm<ForestLeafType, ForestLeafType>() {

    override val items = listOf(
        Item(NEEDLELEAVED, R.drawable.leaf_type_needleleaved, R.string.quest_leaf_type_needles),
        Item(BROADLEAVED, R.drawable.leaf_type_broadleaved, R.string.quest_leaf_type_broadleaved),
        Item(MIXED, R.drawable.leaf_type_mixed, R.string.quest_leaf_type_mixed)
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<ForestLeafType>) {
        applyAnswer(selectedItems.single())
    }
}
