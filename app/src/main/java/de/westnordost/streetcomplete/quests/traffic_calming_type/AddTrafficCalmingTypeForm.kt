package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AImageListQuestForm
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.BUMP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CHICANE
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CHOKER
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CUSHION
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.HUMP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.ISLAND
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.RUMBLE_STRIP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.TABLE
import de.westnordost.streetcomplete.view.image_select.Item

class AddTrafficCalmingTypeForm : AImageListQuestForm<TrafficCalmingType, TrafficCalmingType>() {

    override val items = listOf(
        Item(BUMP, R.drawable.traffic_calming_bump, R.string.quest_traffic_calming_type_bump),
        Item(HUMP, R.drawable.traffic_calming_hump, R.string.quest_traffic_calming_type_hump),
        Item(TABLE, R.drawable.traffic_calming_table, R.string.quest_traffic_calming_type_table),
        Item(CUSHION, R.drawable.traffic_calming_cushion, R.string.quest_traffic_calming_type_cushion),
        Item(ISLAND, R.drawable.traffic_calming_island, R.string.quest_traffic_calming_type_island),
        Item(CHOKER, R.drawable.traffic_calming_choker, R.string.quest_traffic_calming_type_choker),
        Item(CHICANE, R.drawable.traffic_calming_chicane, R.string.quest_traffic_calming_type_chicane),
        Item(RUMBLE_STRIP, R.drawable.traffic_calming_rumble_strip, R.string.quest_traffic_calming_type_rumble_strip),
    )

    override val itemsPerRow = 3

    override fun onClickOk(selectedItems: List<TrafficCalmingType>) {
        applyAnswer(selectedItems.single())
    }
}
