package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.quests.barrier_type.StileTypeAnswer.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.barrier_gate_pedestrian
import de.westnordost.streetcomplete.resources.barrier_kissing_gate
import de.westnordost.streetcomplete.resources.barrier_passage
import de.westnordost.streetcomplete.resources.barrier_stile_ladder
import de.westnordost.streetcomplete.resources.barrier_stile_squeezer
import de.westnordost.streetcomplete.resources.barrier_stile_stepover_stone
import de.westnordost.streetcomplete.resources.barrier_stile_stepover_wooden
import de.westnordost.streetcomplete.resources.quest_barrier_type_gate_conversion
import de.westnordost.streetcomplete.resources.quest_barrier_type_kissing_gate_conversion
import de.westnordost.streetcomplete.resources.quest_barrier_type_passage_conversion
import de.westnordost.streetcomplete.resources.quest_barrier_type_stepover_stone
import de.westnordost.streetcomplete.resources.quest_barrier_type_stepover_wooden
import de.westnordost.streetcomplete.resources.quest_barrier_type_stile_ladder
import de.westnordost.streetcomplete.resources.quest_barrier_type_stile_squeezer
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val StileTypeAnswer.title: StringResource get() = when (this) {
    KISSING_GATE ->      Res.string.quest_barrier_type_kissing_gate_conversion
    PASSAGE ->           Res.string.quest_barrier_type_passage_conversion
    GATE ->              Res.string.quest_barrier_type_gate_conversion
    SQUEEZER ->          Res.string.quest_barrier_type_stile_squeezer
    LADDER ->            Res.string.quest_barrier_type_stile_ladder
    STEPOVER_WOODEN ->   Res.string.quest_barrier_type_stepover_wooden
    STEPOVER_STONE ->    Res.string.quest_barrier_type_stepover_stone
}

val StileTypeAnswer.icon: DrawableResource get() = when (this) {
    KISSING_GATE ->      Res.drawable.barrier_kissing_gate
    PASSAGE ->           Res.drawable.barrier_passage
    GATE ->              Res.drawable.barrier_gate_pedestrian
    SQUEEZER ->          Res.drawable.barrier_stile_squeezer
    LADDER ->            Res.drawable.barrier_stile_ladder
    STEPOVER_WOODEN ->   Res.drawable.barrier_stile_stepover_wooden
    STEPOVER_STONE ->    Res.drawable.barrier_stile_stepover_stone
}
