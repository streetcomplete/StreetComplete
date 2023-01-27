package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBollardTypeForm : AImageListQuestForm<BollardType, BollardTypeAnswer>() {

    override val items = BollardType.values().map { it.asItem() }
    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BollardType>) {
        applyAnswer(selectedItems.single())
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_bollard_type_not_bollard) {
            applyAnswer(BarrierTypeIsNotBollard)
        },
    )
}
