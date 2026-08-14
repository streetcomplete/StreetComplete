package de.westnordost.streetcomplete.osm.traffic_calming

import de.westnordost.streetcomplete.osm.traffic_calming.LaneNarrowingTrafficCalming.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val LaneNarrowingTrafficCalming.icon: DrawableResource get() = when (this) {
    CHOKER -> Res.drawable.lane_narrowing_traffic_calming_choker
    ISLAND -> Res.drawable.lane_narrowing_traffic_calming_island
    CHICANE -> Res.drawable.lane_narrowing_traffic_calming_chicane
    CHOKED_ISLAND -> Res.drawable.lane_narrowing_traffic_calming_choked_island
}

val LaneNarrowingTrafficCalming.title: StringResource get() = when (this) {
    CHOKER -> Res.string.lane_narrowing_traffic_calming_choker
    ISLAND -> Res.string.lane_narrowing_traffic_calming_island
    CHICANE -> Res.string.lane_narrowing_traffic_calming_chicane
    CHOKED_ISLAND -> Res.string.lane_narrowing_traffic_calming_choked_island
}
