package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddFireHydrantPositionForm : AImageListQuestForm<FireHydrantPosition, FireHydrantPosition>() {

    override val items: List<DisplayItem<FireHydrantPosition>> get() {
        val isPillar = element.tags["fire_hydrant:type"] == "pillar"
        return FireHydrantPosition.values().map { it.asItem(isPillar) }
    }

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
