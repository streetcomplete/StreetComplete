package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.quests.AImageListQuestComposeForm

class AddTrafficCalmingTypeForm : AImageListQuestComposeForm<TrafficCalmingType, TrafficCalmingType>() {

    override val items = TrafficCalmingType.entries.map { it.asItem() }

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<TrafficCalmingType>) {
        applyAnswer(selectedItems.single())
    }
}
