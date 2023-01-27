package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.quests.AImageListQuestForm

class AddTrafficCalmingTypeForm : AImageListQuestForm<TrafficCalmingType, TrafficCalmingType>() {

    override val items = TrafficCalmingType.values().map { it.asItem() }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<TrafficCalmingType>) {
        applyAnswer(selectedItems.single())
    }
}
