package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.barrier_type.StileType.LADDER
import de.westnordost.streetcomplete.quests.barrier_type.StileType.SQUEEZER
import de.westnordost.streetcomplete.quests.barrier_type.StileType.STEPOVER_STONE
import de.westnordost.streetcomplete.quests.barrier_type.StileType.STEPOVER_WOODEN
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

class AddStileTypeForm : AImageListQuestForm<StileTypeAnswer, StileTypeAnswer>() {

    override val items: List<DisplayItem<StileTypeAnswer>> = listOf(
        Item(SQUEEZER, R.drawable.barrier_stile_squeezer, R.string.quest_barrier_type_stile_squeezer),
        Item(LADDER, R.drawable.barrier_stile_ladder, R.string.quest_barrier_type_stile_ladder),
        Item(STEPOVER_WOODEN, R.drawable.barrier_stile_stepover_wooden, R.string.quest_barrier_type_stepover_wooden),
        Item(STEPOVER_STONE, R.drawable.barrier_stile_stepover_stone, R.string.quest_barrier_type_stepover_stone),
        Item(ConvertedStile.KISSING_GATE, R.drawable.barrier_kissing_gate, R.string.quest_barrier_type_kissing_gate_conversion),
        Item(ConvertedStile.PASSAGE, R.drawable.barrier_passage, R.string.quest_barrier_type_passage_conversion),
        Item(ConvertedStile.GATE, R.drawable.barrier_gate_pedestrian, R.string.quest_barrier_type_gate_conversion),
    )
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<StileTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
