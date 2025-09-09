package de.westnordost.streetcomplete.quests.traffic_calming_type

import de.westnordost.streetcomplete.quests.traffic_calming_type.TrafficCalmingType.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_bump
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_chicane
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_choker
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_cushion
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_hump
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_island
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_rumble_strip
import de.westnordost.streetcomplete.resources.quest_traffic_calming_type_table
import de.westnordost.streetcomplete.resources.traffic_calming_bump
import de.westnordost.streetcomplete.resources.traffic_calming_chicane
import de.westnordost.streetcomplete.resources.traffic_calming_choker
import de.westnordost.streetcomplete.resources.traffic_calming_cushion
import de.westnordost.streetcomplete.resources.traffic_calming_hump
import de.westnordost.streetcomplete.resources.traffic_calming_island
import de.westnordost.streetcomplete.resources.traffic_calming_rumble_strip
import de.westnordost.streetcomplete.resources.traffic_calming_table
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val TrafficCalmingType.title: StringResource get() = when (this) {
    BUMP ->         Res.string.quest_traffic_calming_type_bump
    HUMP ->         Res.string.quest_traffic_calming_type_hump
    TABLE ->        Res.string.quest_traffic_calming_type_table
    CUSHION ->      Res.string.quest_traffic_calming_type_cushion
    ISLAND ->       Res.string.quest_traffic_calming_type_island
    CHOKER ->       Res.string.quest_traffic_calming_type_choker
    CHICANE ->      Res.string.quest_traffic_calming_type_chicane
    RUMBLE_STRIP -> Res.string.quest_traffic_calming_type_rumble_strip
}

val TrafficCalmingType.icon: DrawableResource get() = when (this) {
    BUMP ->         Res.drawable.traffic_calming_bump
    HUMP ->         Res.drawable.traffic_calming_hump
    TABLE ->        Res.drawable.traffic_calming_table
    CUSHION ->      Res.drawable.traffic_calming_cushion
    ISLAND ->       Res.drawable.traffic_calming_island
    CHOKER ->       Res.drawable.traffic_calming_choker
    CHICANE ->      Res.drawable.traffic_calming_chicane
    RUMBLE_STRIP -> Res.drawable.traffic_calming_rumble_strip
}
