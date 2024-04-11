package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBicycleBarrierTypeForm :
    AImageListQuestForm<BicycleBarrierType, BicycleBarrierTypeAnswer>() {

    override val items = BicycleBarrierType.entries.map { it.asItem() }
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BicycleBarrierType>) {
        applyAnswer(selectedItems.single())
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_barrier_bicycle_type_not_cycle_barrier) {
            applyAnswer(BarrierTypeIsNotBicycleBarrier)
        },
    )
}
