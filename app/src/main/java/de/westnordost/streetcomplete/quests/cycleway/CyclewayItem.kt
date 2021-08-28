package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.image_select.Item
import de.westnordost.streetcomplete.quests.cycleway.Cycleway.*

fun Cycleway.isAvailableAsSelection(countryCode: String): Boolean =
    !isUnknown && !isInvalid && !isAmbiguous(countryCode) &&
    /* suggestion lanes are only known in Belgium and Netherlands */
    (this != SUGGESTION_LANE || countryCode in listOf("NL", "BE"))

fun Cycleway.asItem(isLeftHandTraffic: Boolean) =
    when(this) {
        NONE -> Item(this, R.drawable.ic_cycleway_none_in_selection, getTitleResId())
        SEPARATE -> Item(this, R.drawable.ic_cycleway_separate, getTitleResId())
        else -> Item(this, getIconResId(isLeftHandTraffic), getTitleResId())
    }

fun Cycleway.getIconResId(isLeftHandTraffic: Boolean): Int =
    if (isLeftHandTraffic) getLeftHandTrafficIconResId() else getRightHandTrafficIconResId()

private fun Cycleway.getRightHandTrafficIconResId(): Int = when(this) {
    UNSPECIFIED_LANE -> R.drawable.ic_cycleway_lane
    EXCLUSIVE_LANE -> R.drawable.ic_cycleway_lane
    ADVISORY_LANE -> R.drawable.ic_cycleway_shared_lane
    SUGGESTION_LANE -> R.drawable.ic_cycleway_suggestion_lane
    TRACK -> R.drawable.ic_cycleway_track
    NONE -> R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY -> R.drawable.ic_cycleway_none_no_oneway
    PICTOGRAMS -> R.drawable.ic_cycleway_pictograms
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit
    DUAL_LANE -> R.drawable.ic_cycleway_lane_dual
    DUAL_TRACK -> R.drawable.ic_cycleway_track_dual
    BUSWAY -> R.drawable.ic_cycleway_bus_lane
    SEPARATE -> R.drawable.ic_cycleway_none
    else -> 0
}

private fun Cycleway.getLeftHandTrafficIconResId(): Int = when(this) {
    UNSPECIFIED_LANE -> R.drawable.ic_cycleway_lane_l
    EXCLUSIVE_LANE -> R.drawable.ic_cycleway_lane_l
    ADVISORY_LANE -> R.drawable.ic_cycleway_shared_lane_l
    SUGGESTION_LANE -> R.drawable.ic_cycleway_suggestion_lane
    TRACK -> R.drawable.ic_cycleway_track_l
    NONE -> R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY -> R.drawable.ic_cycleway_none_no_oneway_l
    PICTOGRAMS -> R.drawable.ic_cycleway_pictograms_l
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit_l
    DUAL_LANE -> R.drawable.ic_cycleway_lane_dual_l
    DUAL_TRACK -> R.drawable.ic_cycleway_track_dual_l
    BUSWAY -> R.drawable.ic_cycleway_bus_lane_l
    SEPARATE -> R.drawable.ic_cycleway_none
    else -> 0
}

fun Cycleway.getTitleResId(): Int = when(this) {
    UNSPECIFIED_LANE -> R.string.quest_cycleway_value_lane
    EXCLUSIVE_LANE -> R.string.quest_cycleway_value_lane
    ADVISORY_LANE -> R.string.quest_cycleway_value_lane_soft
    SUGGESTION_LANE -> R.string.quest_cycleway_value_suggestion_lane
    TRACK -> R.string.quest_cycleway_value_track
    NONE -> R.string.quest_cycleway_value_none
    NONE_NO_ONEWAY -> R.string.quest_cycleway_value_none_but_no_oneway
    PICTOGRAMS -> R.string.quest_cycleway_value_shared
    SIDEWALK_EXPLICIT -> R.string.quest_cycleway_value_sidewalk
    DUAL_LANE -> R.string.quest_cycleway_value_lane_dual
    DUAL_TRACK -> R.string.quest_cycleway_value_track_dual
    BUSWAY -> R.string.quest_cycleway_value_bus_lane
    SEPARATE -> R.string.quest_cycleway_value_separate
    else -> 0
}

val DISPLAYED_CYCLEWAY_ITEMS: List<Cycleway> = listOf(
    NONE,
    TRACK,
    EXCLUSIVE_LANE,
    ADVISORY_LANE,
    UNSPECIFIED_LANE,
    SUGGESTION_LANE,
    SEPARATE,
    PICTOGRAMS,
    BUSWAY,
    SIDEWALK_EXPLICIT,
    DUAL_LANE,
    DUAL_TRACK
)
