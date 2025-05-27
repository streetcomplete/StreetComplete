package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item

fun StileTypeAnswer.asItem() = Item(this, iconResId, titleResId)

private val StileTypeAnswer.titleResId: Int get() = when (this) {
    ConvertedStile.KISSING_GATE -> R.string.quest_barrier_type_kissing_gate_conversion
    ConvertedStile.PASSAGE ->      R.string.quest_barrier_type_passage_conversion
    ConvertedStile.GATE ->         R.string.quest_barrier_type_gate_conversion
    StileType.SQUEEZER ->          R.string.quest_barrier_type_stile_squeezer
    StileType.LADDER ->            R.string.quest_barrier_type_stile_ladder
    StileType.STEPOVER_WOODEN ->   R.string.quest_barrier_type_stepover_wooden
    StileType.STEPOVER_STONE ->    R.string.quest_barrier_type_stepover_stone
}

private val StileTypeAnswer.iconResId: Int get() = when (this) {
    ConvertedStile.KISSING_GATE -> R.drawable.barrier_kissing_gate
    ConvertedStile.PASSAGE ->      R.drawable.barrier_passage
    ConvertedStile.GATE ->         R.drawable.barrier_gate_pedestrian
    StileType.SQUEEZER ->          R.drawable.barrier_stile_squeezer
    StileType.LADDER ->            R.drawable.barrier_stile_ladder
    StileType.STEPOVER_WOODEN ->   R.drawable.barrier_stile_stepover_wooden
    StileType.STEPOVER_STONE ->    R.drawable.barrier_stile_stepover_stone
}
