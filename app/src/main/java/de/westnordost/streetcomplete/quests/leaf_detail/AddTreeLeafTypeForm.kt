package de.westnordost.streetcomplete.quests.leaf_detail

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddTreeLeafTypeForm :
    AImageListQuestForm<TreeLeafType, TreeLeafTypeAnswer>() {

    override val items = TreeLeafType.entries.map { it.asItem() }
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<TreeLeafType>) {
        applyAnswer(selectedItems.single())
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_leafType_tree_is_just_a_stump) {
            applyAnswer(NotTreeButStump)
        },
    )
}
