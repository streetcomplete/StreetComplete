package de.westnordost.streetcomplete.quests.fire_hydrant_position

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm
import de.westnordost.streetcomplete.view.image_select.DisplayItem

class AddFireHydrantPositionForm : AImageListQuestComposeForm<FireHydrantPosition, FireHydrantPosition>() {

    override val items: List<DisplayItem<FireHydrantPosition>> get() {
        val isPillar = element.tags["fire_hydrant:type"] == "pillar"
        return FireHydrantPosition.entries.map { it.asItem(isPillar) }
    }

    override val itemsPerRow = 2

    override fun onClickOk(selectedItems: List<FireHydrantPosition>) {
        applyAnswer(selectedItems.single())
    }
}
