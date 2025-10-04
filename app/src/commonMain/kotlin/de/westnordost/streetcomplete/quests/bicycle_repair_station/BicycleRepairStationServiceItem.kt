package de.westnordost.streetcomplete.quests.bicycle_repair_station

import de.westnordost.streetcomplete.quests.bicycle_repair_station.BicycleRepairStationService.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.bicycle_repair_station_chain_tool
import de.westnordost.streetcomplete.resources.bicycle_repair_station_pump
import de.westnordost.streetcomplete.resources.bicycle_repair_station_stand
import de.westnordost.streetcomplete.resources.bicycle_repair_station_tools
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_station_chain_tool
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_station_pump
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_station_stand
import de.westnordost.streetcomplete.resources.quest_bicycle_repair_station_tools
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val BicycleRepairStationService.title: StringResource get() = when (this) {
    PUMP ->       Res.string.quest_bicycle_repair_station_pump
    TOOLS ->      Res.string.quest_bicycle_repair_station_tools
    STAND ->      Res.string.quest_bicycle_repair_station_stand
    CHAIN_TOOL -> Res.string.quest_bicycle_repair_station_chain_tool
}

val BicycleRepairStationService.icon: DrawableResource get() = when (this) {
    PUMP ->       Res.drawable.bicycle_repair_station_pump
    TOOLS ->      Res.drawable.bicycle_repair_station_tools
    STAND ->      Res.drawable.bicycle_repair_station_stand
    CHAIN_TOOL -> Res.drawable.bicycle_repair_station_chain_tool
}
