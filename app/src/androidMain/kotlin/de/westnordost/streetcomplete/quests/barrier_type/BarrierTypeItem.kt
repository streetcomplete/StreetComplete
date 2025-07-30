package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.BICYCLE_BARRIER
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.BLOCK
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.BOLLARD
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.CATTLE_GRID
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.CHAIN
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.DEBRIS_PILE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.FULL_HEIGHT_TURNSTILE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.GATE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.HEIGHT_RESTRICTOR
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.JERSEY_BARRIER
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.KERB
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.KISSING_GATE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.LIFT_GATE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.LOG
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.PASSAGE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.ROPE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.STILE_LADDER
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.STILE_SQUEEZER
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.STILE_STEPOVER_STONE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.STILE_STEPOVER_WOODEN
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.SWING_GATE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.TURNSTILE
import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.WIRE_GATE
import de.westnordost.streetcomplete.view.image_select.Item

fun BarrierType.asItem() = Item(this, iconResId, titleResId)

private val BarrierType.titleResId: Int get() = when (this) {
    PASSAGE ->               R.string.quest_barrier_type_passage
    GATE ->                  R.string.quest_barrier_type_gate_any_size
    LIFT_GATE ->             R.string.quest_barrier_type_lift_gate
    SWING_GATE ->            R.string.quest_barrier_type_swing_gate
    BOLLARD ->               R.string.quest_barrier_type_bollard
    CHAIN ->                 R.string.quest_barrier_type_chain
    ROPE ->                  R.string.quest_barrier_type_rope
    WIRE_GATE ->             R.string.quest_barrier_type_wire_gate
    CATTLE_GRID ->           R.string.quest_barrier_type_cattle_grid
    BLOCK ->                 R.string.quest_barrier_type_block
    JERSEY_BARRIER ->        R.string.quest_barrier_jersey_barrier
    LOG ->                   R.string.quest_barrier_type_log
    KERB ->                  R.string.quest_barrier_type_kerb
    HEIGHT_RESTRICTOR ->     R.string.quest_barrier_type_height_restrictor
    FULL_HEIGHT_TURNSTILE -> R.string.quest_barrier_full_height_turnstile
    TURNSTILE ->             R.string.quest_barrier_type_turnstile
    DEBRIS_PILE ->           R.string.quest_barrier_type_debris_pile
    STILE_SQUEEZER ->        R.string.quest_barrier_type_stile_squeezer
    STILE_LADDER ->          R.string.quest_barrier_type_stile_ladder
    STILE_STEPOVER_WOODEN -> R.string.quest_barrier_type_stepover_wooden
    STILE_STEPOVER_STONE ->  R.string.quest_barrier_type_stepover_stone
    KISSING_GATE ->          R.string.quest_barrier_type_kissing_gate
    BICYCLE_BARRIER ->       R.string.quest_barrier_type_bicycle_barrier
}

private val BarrierType.iconResId: Int get() = when (this) {
    PASSAGE ->               R.drawable.barrier_passage
    GATE ->                  R.drawable.barrier_gate
    LIFT_GATE ->             R.drawable.barrier_lift_gate
    SWING_GATE ->            R.drawable.barrier_swing_gate
    BOLLARD ->               R.drawable.barrier_bollard
    CHAIN ->                 R.drawable.barrier_chain
    ROPE ->                  R.drawable.barrier_rope
    WIRE_GATE ->             R.drawable.barrier_wire_gate
    CATTLE_GRID ->           R.drawable.barrier_cattle_grid
    BLOCK ->                 R.drawable.barrier_block
    JERSEY_BARRIER ->        R.drawable.barrier_jersey_barrier
    LOG ->                   R.drawable.barrier_log
    KERB ->                  R.drawable.barrier_kerb
    HEIGHT_RESTRICTOR ->     R.drawable.barrier_height_restrictor
    FULL_HEIGHT_TURNSTILE -> R.drawable.barrier_full_height_turnstile
    TURNSTILE ->             R.drawable.barrier_turnstile
    DEBRIS_PILE ->           R.drawable.barrier_debris_pile
    STILE_SQUEEZER ->        R.drawable.barrier_stile_squeezer
    STILE_LADDER ->          R.drawable.barrier_stile_ladder
    STILE_STEPOVER_WOODEN -> R.drawable.barrier_stile_stepover_wooden
    STILE_STEPOVER_STONE ->  R.drawable.barrier_stile_stepover_stone
    KISSING_GATE ->          R.drawable.barrier_kissing_gate
    BICYCLE_BARRIER ->       R.drawable.barrier_bicycle_barrier
}
