package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddStileTypeForm : AImageListQuestForm<StileTypeAnswer, StileTypeAnswer>() {

    override val items =
        StileType.entries.map { it.asItem() } +
        ConvertedStile.entries.map { it.asItem() }

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<StileTypeAnswer>) {
        applyAnswer(selectedItems.single())
    }
}
