package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.quests.barrier_type.BarrierType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.barrier_bicycle_barrier
import de.westnordost.streetcomplete.resources.barrier_block
import de.westnordost.streetcomplete.resources.barrier_bollard
import de.westnordost.streetcomplete.resources.barrier_cattle_grid
import de.westnordost.streetcomplete.resources.barrier_chain
import de.westnordost.streetcomplete.resources.barrier_debris_pile
import de.westnordost.streetcomplete.resources.barrier_full_height_turnstile
import de.westnordost.streetcomplete.resources.barrier_gate
import de.westnordost.streetcomplete.resources.barrier_height_restrictor
import de.westnordost.streetcomplete.resources.barrier_jersey_barrier
import de.westnordost.streetcomplete.resources.barrier_kerb
import de.westnordost.streetcomplete.resources.barrier_kissing_gate
import de.westnordost.streetcomplete.resources.barrier_lift_gate
import de.westnordost.streetcomplete.resources.barrier_log
import de.westnordost.streetcomplete.resources.barrier_passage
import de.westnordost.streetcomplete.resources.barrier_rope
import de.westnordost.streetcomplete.resources.barrier_stile_ladder
import de.westnordost.streetcomplete.resources.barrier_stile_squeezer
import de.westnordost.streetcomplete.resources.barrier_stile_stepover_stone
import de.westnordost.streetcomplete.resources.barrier_stile_stepover_wooden
import de.westnordost.streetcomplete.resources.barrier_swing_gate
import de.westnordost.streetcomplete.resources.barrier_turnstile
import de.westnordost.streetcomplete.resources.barrier_wire_gate
import de.westnordost.streetcomplete.resources.quest_barrier_full_height_turnstile
import de.westnordost.streetcomplete.resources.quest_barrier_jersey_barrier
import de.westnordost.streetcomplete.resources.quest_barrier_type_bicycle_barrier
import de.westnordost.streetcomplete.resources.quest_barrier_type_block
import de.westnordost.streetcomplete.resources.quest_barrier_type_bollard
import de.westnordost.streetcomplete.resources.quest_barrier_type_cattle_grid
import de.westnordost.streetcomplete.resources.quest_barrier_type_chain
import de.westnordost.streetcomplete.resources.quest_barrier_type_debris_pile
import de.westnordost.streetcomplete.resources.quest_barrier_type_gate_any_size
import de.westnordost.streetcomplete.resources.quest_barrier_type_height_restrictor
import de.westnordost.streetcomplete.resources.quest_barrier_type_kerb
import de.westnordost.streetcomplete.resources.quest_barrier_type_kissing_gate
import de.westnordost.streetcomplete.resources.quest_barrier_type_lift_gate
import de.westnordost.streetcomplete.resources.quest_barrier_type_log
import de.westnordost.streetcomplete.resources.quest_barrier_type_passage
import de.westnordost.streetcomplete.resources.quest_barrier_type_rope
import de.westnordost.streetcomplete.resources.quest_barrier_type_stepover_stone
import de.westnordost.streetcomplete.resources.quest_barrier_type_stepover_wooden
import de.westnordost.streetcomplete.resources.quest_barrier_type_stile_ladder
import de.westnordost.streetcomplete.resources.quest_barrier_type_stile_squeezer
import de.westnordost.streetcomplete.resources.quest_barrier_type_swing_gate
import de.westnordost.streetcomplete.resources.quest_barrier_type_turnstile
import de.westnordost.streetcomplete.resources.quest_barrier_type_wire_gate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BarrierType.title: StringResource get() = when (this) {
    PASSAGE ->               Res.string.quest_barrier_type_passage
    GATE ->                  Res.string.quest_barrier_type_gate_any_size
    LIFT_GATE ->             Res.string.quest_barrier_type_lift_gate
    SWING_GATE ->            Res.string.quest_barrier_type_swing_gate
    BOLLARD ->               Res.string.quest_barrier_type_bollard
    CHAIN ->                 Res.string.quest_barrier_type_chain
    ROPE ->                  Res.string.quest_barrier_type_rope
    WIRE_GATE ->             Res.string.quest_barrier_type_wire_gate
    CATTLE_GRID ->           Res.string.quest_barrier_type_cattle_grid
    BLOCK ->                 Res.string.quest_barrier_type_block
    JERSEY_BARRIER ->        Res.string.quest_barrier_jersey_barrier
    LOG ->                   Res.string.quest_barrier_type_log
    KERB ->                  Res.string.quest_barrier_type_kerb
    HEIGHT_RESTRICTOR ->     Res.string.quest_barrier_type_height_restrictor
    FULL_HEIGHT_TURNSTILE -> Res.string.quest_barrier_full_height_turnstile
    TURNSTILE ->             Res.string.quest_barrier_type_turnstile
    DEBRIS_PILE ->           Res.string.quest_barrier_type_debris_pile
    STILE_SQUEEZER ->        Res.string.quest_barrier_type_stile_squeezer
    STILE_LADDER ->          Res.string.quest_barrier_type_stile_ladder
    STILE_STEPOVER_WOODEN -> Res.string.quest_barrier_type_stepover_wooden
    STILE_STEPOVER_STONE ->  Res.string.quest_barrier_type_stepover_stone
    KISSING_GATE ->          Res.string.quest_barrier_type_kissing_gate
    BICYCLE_BARRIER ->       Res.string.quest_barrier_type_bicycle_barrier
}

val BarrierType.icon: DrawableResource get() = when (this) {
    PASSAGE ->               Res.drawable.barrier_passage
    GATE ->                  Res.drawable.barrier_gate
    LIFT_GATE ->             Res.drawable.barrier_lift_gate
    SWING_GATE ->            Res.drawable.barrier_swing_gate
    BOLLARD ->               Res.drawable.barrier_bollard
    CHAIN ->                 Res.drawable.barrier_chain
    ROPE ->                  Res.drawable.barrier_rope
    WIRE_GATE ->             Res.drawable.barrier_wire_gate
    CATTLE_GRID ->           Res.drawable.barrier_cattle_grid
    BLOCK ->                 Res.drawable.barrier_block
    JERSEY_BARRIER ->        Res.drawable.barrier_jersey_barrier
    LOG ->                   Res.drawable.barrier_log
    KERB ->                  Res.drawable.barrier_kerb
    HEIGHT_RESTRICTOR ->     Res.drawable.barrier_height_restrictor
    FULL_HEIGHT_TURNSTILE -> Res.drawable.barrier_full_height_turnstile
    TURNSTILE ->             Res.drawable.barrier_turnstile
    DEBRIS_PILE ->           Res.drawable.barrier_debris_pile
    STILE_SQUEEZER ->        Res.drawable.barrier_stile_squeezer
    STILE_LADDER ->          Res.drawable.barrier_stile_ladder
    STILE_STEPOVER_WOODEN -> Res.drawable.barrier_stile_stepover_wooden
    STILE_STEPOVER_STONE ->  Res.drawable.barrier_stile_stepover_stone
    KISSING_GATE ->          Res.drawable.barrier_kissing_gate
    BICYCLE_BARRIER ->       Res.drawable.barrier_bicycle_barrier
}
