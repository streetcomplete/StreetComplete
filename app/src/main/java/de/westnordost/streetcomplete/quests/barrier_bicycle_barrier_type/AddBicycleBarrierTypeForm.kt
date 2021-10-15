package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddBicycleBarrierTypeForm : AImageListQuestAnswerFragment<BicycleBarrierType, BicycleBarrierType>() {

    override val items = listOf(
        Item(SINGLE, R.drawable.barrier_bicycle_barrier_single),
        Item(DOUBLE, R.drawable.barrier_bicycle_barrier_double),
        Item(TRIPLE, R.drawable.barrier_bicycle_barrier_triple),
        Item(DIAGONAL, R.drawable.barrier_bicycle_barrier_diagonal),
        Item(SQUEEZE, R.drawable.barrier_bicycle_barrier_squeeze),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BicycleBarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
