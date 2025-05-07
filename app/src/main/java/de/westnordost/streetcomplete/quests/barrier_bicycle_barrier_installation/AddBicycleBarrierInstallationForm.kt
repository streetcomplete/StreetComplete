package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_installation

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddBicycleBarrierInstallationForm :
    AImageListQuestComposeForm<BicycleBarrierInstallation, BicycleBarrierInstallationAnswer>() {

    override var items = BicycleBarrierInstallation.entries.map { it.asItem() }
    override val itemsPerRow = 3
    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BicycleBarrierInstallation>) {
        applyAnswer(selectedItems.single())
    }

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_barrier_bicycle_type_not_cycle_barrier) {
            applyAnswer(BarrierTypeIsNotBicycleBarrier)
        },
    )
}
