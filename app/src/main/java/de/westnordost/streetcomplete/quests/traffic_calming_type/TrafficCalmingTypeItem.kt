package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.BUMP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CHICANE
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CHOKER
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.CUSHION
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.HUMP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.ISLAND
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.RUMBLE_STRIP
import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.TABLE
import de.westnordost.streetcomplete.view.image_select.Item

fun TrafficCalmingType.asItem() = Item(this, iconResId, titleResId)

private val TrafficCalmingType.titleResId: Int get() = when (this) {
    BUMP ->         R.string.quest_traffic_calming_type_bump
    HUMP ->         R.string.quest_traffic_calming_type_hump
    TABLE ->        R.string.quest_traffic_calming_type_table
    CUSHION ->      R.string.quest_traffic_calming_type_cushion
    ISLAND ->       R.string.quest_traffic_calming_type_island
    CHOKER ->       R.string.quest_traffic_calming_type_choker
    CHICANE ->      R.string.quest_traffic_calming_type_chicane
    RUMBLE_STRIP -> R.string.quest_traffic_calming_type_rumble_strip
}

private val TrafficCalmingType.iconResId: Int get() = when (this) {
    BUMP ->         R.drawable.traffic_calming_bump
    HUMP ->         R.drawable.traffic_calming_hump
    TABLE ->        R.drawable.traffic_calming_table
    CUSHION ->      R.drawable.traffic_calming_cushion
    ISLAND ->       R.drawable.traffic_calming_island
    CHOKER ->       R.drawable.traffic_calming_choker
    CHICANE ->      R.drawable.traffic_calming_chicane
    RUMBLE_STRIP -> R.drawable.traffic_calming_rumble_strip
}
