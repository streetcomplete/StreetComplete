package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestAnswerFragment
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.*
import de.westnordost.streetcomplete.view.image_select.Item

class AddBarrierTypeForm : AImageListQuestAnswerFragment<BarrierType, BarrierType>() {

    override val items = listOf(
        Item(PASSAGE, R.drawable.barrier_passage, R.string.quest_barrier_type_passage),
        Item(GATE, R.drawable.barrier_gate, R.string.quest_barrier_type_gate),
        Item(LIFT_GATE, R.drawable.barrier_lift_gate, R.string.quest_barrier_type_lift_gate),
        Item(SWING_GATE, R.drawable.barrier_swing_gate, R.string.quest_barrier_type_swing_gate),
        Item(BOLLARD, R.drawable.barrier_bollard, R.string.quest_barrier_type_bollard),
        Item(CHAIN, R.drawable.barrier_chain, R.string.quest_barrier_type_chain),
        Item(ROPE, R.drawable.barrier_rope, R.string.quest_barrier_type_rope),
        Item(WIRE_GATE, R.drawable.barrier_wire_gate, R.string.quest_barrier_type_wire_gate),
        Item(CATTLE_GRID, R.drawable.barrier_cattle_grid, R.string.quest_barrier_type_cattle_grid),
        Item(BLOCK, R.drawable.barrier_block, R.string.quest_barrier_type_block),
        Item(JERSEY_BARRIER, R.drawable.barrier_jersey_barrier, R.string.quest_barrier_jersey_barrier),
        Item(LOG, R.drawable.barrier_log, R.string.quest_barrier_type_log),
        Item(KERB, R.drawable.kerb_height_raised, R.string.quest_barrier_type_kerb),
        Item(HEIGHT_RESTRICTOR, R.drawable.barrier_height_restrictor, R.string.quest_barrier_type_height_restrictor),
        Item(FULL_HEIGHT_TURNSTILE, R.drawable.barrier_full_height_turnstile, R.string.quest_barrier_full_height_turnstile),
        Item(TURNSTILE, R.drawable.barrier_turnstile, R.string.quest_barrier_type_turnstile),
        Item(DEBRIS_PILE, R.drawable.barrier_debris_pile, R.string.quest_barrier_type_debris_pile),
        Item(STILE_SQUEEZER, R.drawable.barrier_stile_squeezer, R.string.quest_barrier_type_stile_squeezer),
        Item(STILE_LADDER, R.drawable.barrier_stile_ladder, R.string.quest_barrier_type_stile_ladder),
        Item(STILE_STEPOVER, R.drawable.barrier_stile_stepover, R.string.quest_barrier_type_stepover),
        Item(KISSING_GATE, R.drawable.barrier_kissing_gate, R.string.quest_barrier_type_kissing_gate), // more clear short description would be better :(
        Item(BICYCLE_BARRIER, R.drawable.barrier_bicycle_barrier, R.string.quest_barrier_type_bicycle_barrier),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
