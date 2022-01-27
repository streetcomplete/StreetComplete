package de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.barrier_bicycle_barrier_type.BicycleBarrierType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddBicycleBarrierTypeForm : AImageListQuestAnswerFragment<BicycleBarrierType, BicycleBarrierType>() {

    override val items = listOf(
        Item(SINGLE, R.drawable.barrier_bicycle_barrier_single, R.string.quest_barrier_bicycle_type_single),
        Item(DOUBLE, R.drawable.barrier_bicycle_barrier_double, R.string.quest_barrier_bicycle_type_double),
        Item(TRIPLE, R.drawable.barrier_bicycle_barrier_triple, R.string.quest_barrier_bicycle_type_multiple),
        Item(DIAGONAL, R.drawable.barrier_bicycle_barrier_diagonal, R.string.quest_barrier_bicycle_type_diagonal),
        Item(TILTED, R.drawable.barrier_bicycle_barrier_tilted, R.string.quest_barrier_bicycle_type_tilted),
    )

    override val itemsPerRow = 3

    override val moveFavoritesToFront = false

    override fun onClickOk(selectedItems: List<BicycleBarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
