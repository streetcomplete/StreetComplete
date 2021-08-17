package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddStileTypeForm : AImageListQuestAnswerFragment<BarrierType, BarrierType>() {

    override val items = listOf(
        Item(STILE_SQUEEZER, R.drawable.barrier_stile_squeezer, R.string.quest_barrier_type_stile_squeezer),
        Item(STILE_LADDER, R.drawable.barrier_stile_ladder, R.string.quest_barrier_type_stile_ladder),
        Item(STILE_STEPOVER_WOODEN, R.drawable.barrier_stile_stepover_wooden, R.string.quest_barrier_type_stepover_wooden),
        Item(STILE_STEPOVER_STONE, R.drawable.barrier_stile_stepover_stone, R.string.quest_barrier_type_stepover_stone),
        Item(KISSING_GATE, R.drawable.barrier_kissing_gate, R.string.quest_barrier_type_kissing_gate_conversion),
    )

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<BarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
