package de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.lane_narrowing_traffic_calming.LaneNarrowingTrafficCalming.*
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun LaneNarrowingTrafficCalming.asItem(): DisplayItem<LaneNarrowingTrafficCalming> =
    Item(this, iconResId, titleResId)

private val LaneNarrowingTrafficCalming.iconResId: Int get() = when (this) {
    CHOKER -> R.drawable.lane_narrowing_traffic_calming_choker
    ISLAND -> R.drawable.lane_narrowing_traffic_calming_island
    CHICANE -> R.drawable.lane_narrowing_traffic_calming_chicane
    CHOKED_ISLAND -> R.drawable.lane_narrowing_traffic_calming_choked_island
}

private val LaneNarrowingTrafficCalming.titleResId: Int get() = when (this) {
    CHOKER -> R.string.lane_narrowing_traffic_calming_choker
    ISLAND -> R.string.lane_narrowing_traffic_calming_island
    CHICANE -> R.string.lane_narrowing_traffic_calming_chicane
    CHOKED_ISLAND -> R.string.lane_narrowing_traffic_calming_choked_island
}
