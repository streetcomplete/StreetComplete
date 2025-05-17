package de.westnordost.streetcomplete.quests.bicycle_repair_station

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService.*
import de.westnordost.streetcomplete.view.image_select.Item

fun BicycleRepairStationService.asItem(): Item<BicycleRepairStationService> =
    Item(this, iconResId, titleResId)

val BicycleRepairStationService.titleResId: Int get() = when (this) {
    PUMP -> R.string.quest_bicycle_repair_station_pump
    TOOLS -> R.string.quest_bicycle_repair_station_tools
    STAND -> R.string.quest_bicycle_repair_station_stand
    CHAIN_TOOL -> R.string.quest_bicycle_repair_station_chain_tool
}

val BicycleRepairStationService.iconResId: Int get() = when (this) {
    PUMP -> R.drawable.bicycle_repair_station_pump
    TOOLS -> R.drawable.bicycle_repair_station_tools
    STAND -> R.drawable.bicycle_repair_station_stand
    CHAIN_TOOL -> R.drawable.bicycle_repair_station_chain_tool
}
