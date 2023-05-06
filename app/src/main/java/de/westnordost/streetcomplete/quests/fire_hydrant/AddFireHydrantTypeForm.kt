package de.westnordost.streetcomplete.quests.fire_hydrant

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddFireHydrantTypeForm : AImageListQuestForm<FireHydrantType, FireHydrantType>() {

    override val items = FireHydrantType.values().map { it.asItem() }
    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantType>) {
        applyAnswer(selectedItems.single())
    }
}
