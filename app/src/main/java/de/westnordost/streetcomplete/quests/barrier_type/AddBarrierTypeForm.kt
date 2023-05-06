package de.westnordost.streetcomplete.quests.barrier_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddBarrierTypeForm : AImageListQuestForm<BarrierType, BarrierType>() {

    override val items = BarrierType.values().map { it.asItem() }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<BarrierType>) {
        applyAnswer(selectedItems.single())
    }
}
