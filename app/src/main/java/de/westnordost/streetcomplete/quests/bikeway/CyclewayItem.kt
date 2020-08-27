package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.Item

fun Cycleway.asItem(isLeftHandTraffic: Boolean) : Item<Cycleway> =
    Item(this, getIconResId(isLeftHandTraffic), getTitleResId())

fun Cycleway.getIconResId(isLeftHandTraffic: Boolean): Int =
    if (isLeftHandTraffic) getLeftHandTrafficIconResId() else getRightHandTrafficIconResId()

private fun Cycleway.getRightHandTrafficIconResId(): Int = when(this) {
    Cycleway.LANE_UNSPECIFIED -> R.drawable.ic_cycleway_lane
    Cycleway.EXCLUSIVE_LANE -> R.drawable.ic_cycleway_lane
    Cycleway.ADVISORY_LANE -> R.drawable.ic_cycleway_shared_lane
    Cycleway.SUGGESTION_LANE -> R.drawable.ic_cycleway_suggestion_lane
    Cycleway.TRACK -> R.drawable.ic_cycleway_track
    Cycleway.NONE -> R.drawable.ic_cycleway_none
    Cycleway.NONE_NO_ONEWAY -> R.drawable.ic_cycleway_pictograms
    Cycleway.PICTOGRAMS -> R.drawable.ic_cycleway_pictograms
    Cycleway.SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit
    Cycleway.SIDEWALK_OK -> R.drawable.ic_cycleway_sidewalk
    Cycleway.DUAL_LANE -> R.drawable.ic_cycleway_lane_dual
    Cycleway.DUAL_TRACK -> R.drawable.ic_cycleway_track_dual
    Cycleway.BUSWAY -> R.drawable.ic_cycleway_bus_lane
}

private fun Cycleway.getLeftHandTrafficIconResId(): Int = when(this) {
    Cycleway.LANE_UNSPECIFIED -> R.drawable.ic_cycleway_lane_l
    Cycleway.EXCLUSIVE_LANE -> R.drawable.ic_cycleway_lane_l
    Cycleway.ADVISORY_LANE -> R.drawable.ic_cycleway_shared_lane_l
    Cycleway.SUGGESTION_LANE -> R.drawable.ic_cycleway_suggestion_lane
    Cycleway.TRACK -> R.drawable.ic_cycleway_track_l
    Cycleway.NONE -> R.drawable.ic_cycleway_none
    Cycleway.NONE_NO_ONEWAY -> R.drawable.ic_cycleway_pictograms_l
    Cycleway.PICTOGRAMS -> R.drawable.ic_cycleway_pictograms_l
    Cycleway.SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit_l
    Cycleway.SIDEWALK_OK -> R.drawable.ic_cycleway_sidewalk
    Cycleway.DUAL_LANE -> R.drawable.ic_cycleway_lane_dual_l
    Cycleway.DUAL_TRACK -> R.drawable.ic_cycleway_track_dual_l
    Cycleway.BUSWAY -> R.drawable.ic_cycleway_bus_lane_l
}

private fun Cycleway.getTitleResId(): Int = when(this) {
    Cycleway.LANE_UNSPECIFIED -> R.string.quest_cycleway_value_lane
    Cycleway.EXCLUSIVE_LANE -> R.string.quest_cycleway_value_lane
    Cycleway.ADVISORY_LANE -> R.string.quest_cycleway_value_lane_soft
    Cycleway.SUGGESTION_LANE -> R.string.quest_cycleway_value_suggestion_lane
    Cycleway.TRACK -> R.string.quest_cycleway_value_track
    Cycleway.NONE -> R.string.quest_cycleway_value_none
    Cycleway.NONE_NO_ONEWAY -> R.string.quest_cycleway_value_none_but_no_oneway
    Cycleway.PICTOGRAMS -> R.string.quest_cycleway_value_shared
    Cycleway.SIDEWALK_EXPLICIT -> R.string.quest_cycleway_value_sidewalk
    Cycleway.SIDEWALK_OK -> R.string.quest_cycleway_value_sidewalk_allowed
    Cycleway.DUAL_LANE -> R.string.quest_cycleway_value_lane_dual
    Cycleway.DUAL_TRACK -> R.string.quest_cycleway_value_track_dual
    Cycleway.BUSWAY -> R.string.quest_cycleway_value_bus_lane
}