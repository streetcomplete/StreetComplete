package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.oneway.Direction.*
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.cycleway_bus_lane
import de.westnordost.streetcomplete.resources.cycleway_bus_lane_l
import de.westnordost.streetcomplete.resources.cycleway_none
import de.westnordost.streetcomplete.resources.cycleway_none_in_selection
import de.westnordost.streetcomplete.resources.cycleway_none_no_oneway
import de.westnordost.streetcomplete.resources.cycleway_none_no_oneway_l
import de.westnordost.streetcomplete.resources.cycleway_separate
import de.westnordost.streetcomplete.resources.cycleway_shoulder
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_explicit
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_explicit_dual
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_explicit_l
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_ok
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_ok_both
import de.westnordost.streetcomplete.resources.cycleway_sidewalk_ok_l
import de.westnordost.streetcomplete.resources.cycleway_track
import de.westnordost.streetcomplete.resources.cycleway_track_dual
import de.westnordost.streetcomplete.resources.cycleway_track_dual_l
import de.westnordost.streetcomplete.resources.cycleway_track_l
import de.westnordost.streetcomplete.resources.floating_separate
import de.westnordost.streetcomplete.resources.quest_cycleway_value_advisory_lane
import de.westnordost.streetcomplete.resources.quest_cycleway_value_bus_lane
import de.westnordost.streetcomplete.resources.quest_cycleway_value_lane
import de.westnordost.streetcomplete.resources.quest_cycleway_value_lane_dual
import de.westnordost.streetcomplete.resources.quest_cycleway_value_none
import de.westnordost.streetcomplete.resources.quest_cycleway_value_none_and_oneway
import de.westnordost.streetcomplete.resources.quest_cycleway_value_none_but_no_oneway
import de.westnordost.streetcomplete.resources.quest_cycleway_value_separate
import de.westnordost.streetcomplete.resources.quest_cycleway_value_shared
import de.westnordost.streetcomplete.resources.quest_cycleway_value_shoulder
import de.westnordost.streetcomplete.resources.quest_cycleway_value_sidewalk2
import de.westnordost.streetcomplete.resources.quest_cycleway_value_sidewalk_dual2
import de.westnordost.streetcomplete.resources.quest_cycleway_value_sidewalk_ok
import de.westnordost.streetcomplete.resources.quest_cycleway_value_sidewalk_ok_dual
import de.westnordost.streetcomplete.resources.quest_cycleway_value_track
import de.westnordost.streetcomplete.resources.quest_cycleway_value_track_dual
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneMirroredDrawable
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneDrawable
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneMirroredDrawable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

// TODO dialog icon needs to be rotated 180 degrees if countryInfo.isLeftHandTraffic == true

fun CyclewayAndDirection.getDialogIcon(
    isRight: Boolean,
    countryInfo: CountryInfo
): DrawableResource? =
    when (cycleway) {
        NONE ->     Res.drawable.cycleway_none_in_selection
        SEPARATE -> Res.drawable.cycleway_separate
        else ->     getIcon(isRight, countryInfo)
    }

fun Cycleway.getFloatingIcon(
    isContraflowInOneway: Boolean,
    noEntrySignDrawable: DrawableResource
): DrawableResource? =
    when (this) {
        NONE ->     if (isContraflowInOneway) noEntrySignDrawable else null
        SEPARATE -> Res.drawable.floating_separate
        else ->     null
    }

fun CyclewayAndDirection.getIcon(
    isRight: Boolean,
    countryInfo: CountryInfo
): DrawableResource? =
    when (direction) {
        BOTH -> cycleway.getDualTrafficIcon(countryInfo)
        else -> {
            val isForward = (direction == FORWARD)
            val showMirrored = isForward xor isRight
            if (showMirrored) {
                cycleway.getLeftHandTrafficIcon(countryInfo)
            } else {
                cycleway.getRightHandTrafficIcon(countryInfo)
            }
        }
    }

private fun Cycleway.getDualTrafficIcon(countryInfo: CountryInfo): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE, EXCLUSIVE_LANE ->
            if (countryInfo.isLeftHandTraffic) {
                countryInfo.dualCycleLaneMirroredDrawable
            } else {
                countryInfo.dualCycleLaneDrawable
            }
        TRACK ->
            if (countryInfo.isLeftHandTraffic) {
                Res.drawable.cycleway_track_dual_l
            } else {
                Res.drawable.cycleway_track_dual
            }
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit_dual
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk_ok_both
        else ->              null
    }

private fun Cycleway.getRightHandTrafficIcon(countryInfo: CountryInfo): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneDrawable
        EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneDrawable
        ADVISORY_LANE ->     countryInfo.advisoryCycleLaneDrawable
        SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneDrawable
        TRACK ->             Res.drawable.cycleway_track
        NONE ->              Res.drawable.cycleway_none
        NONE_NO_ONEWAY ->    Res.drawable.cycleway_none_no_oneway
        PICTOGRAMS ->        countryInfo.pictogramCycleLaneDrawable
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk_ok
        BUSWAY ->            Res.drawable.cycleway_bus_lane
        SEPARATE ->          Res.drawable.cycleway_none
        SHOULDER ->          Res.drawable.cycleway_shoulder
        else -> null
    }

private fun Cycleway.getLeftHandTrafficIcon(countryInfo: CountryInfo): DrawableResource? =
    when (this) {
        UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneMirroredDrawable
        EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneMirroredDrawable
        ADVISORY_LANE ->     countryInfo.advisoryCycleLaneMirroredDrawable
        SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneMirroredDrawable
        TRACK ->             Res.drawable.cycleway_track_l
        NONE ->              Res.drawable.cycleway_none
        NONE_NO_ONEWAY ->    Res.drawable.cycleway_none_no_oneway_l
        PICTOGRAMS ->        countryInfo.pictogramCycleLaneMirroredDrawable
        SIDEWALK_EXPLICIT -> Res.drawable.cycleway_sidewalk_explicit_l
        SIDEWALK_OK ->       Res.drawable.cycleway_sidewalk_ok_l
        BUSWAY ->            Res.drawable.cycleway_bus_lane_l
        SEPARATE ->          Res.drawable.cycleway_none
        SHOULDER ->          Res.drawable.cycleway_shoulder
        else -> null
    }

fun CyclewayAndDirection.getTitle(isContraflowInOneway: Boolean): StringResource? =
    when (cycleway) {
        UNSPECIFIED_LANE, EXCLUSIVE_LANE -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_lane_dual
            } else {
                Res.string.quest_cycleway_value_lane
            }
        }
        TRACK -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_track_dual
            } else {
                Res.string.quest_cycleway_value_track
            }
        }
        SIDEWALK_EXPLICIT -> {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_sidewalk_dual2
            } else {
                Res.string.quest_cycleway_value_sidewalk2
            }
        }
        NONE -> {
            if (isContraflowInOneway) {
                Res.string.quest_cycleway_value_none_and_oneway
            } else {
                Res.string.quest_cycleway_value_none
            }
        }
        ADVISORY_LANE,
        SUGGESTION_LANE ->   Res.string.quest_cycleway_value_advisory_lane
        NONE_NO_ONEWAY ->    Res.string.quest_cycleway_value_none_but_no_oneway
        PICTOGRAMS ->        Res.string.quest_cycleway_value_shared
        BUSWAY ->            Res.string.quest_cycleway_value_bus_lane
        SEPARATE ->          Res.string.quest_cycleway_value_separate
        SHOULDER ->          Res.string.quest_cycleway_value_shoulder
        SIDEWALK_OK ->       {
            if (direction == BOTH) {
                Res.string.quest_cycleway_value_sidewalk_ok_dual
            } else {
                Res.string.quest_cycleway_value_sidewalk_ok
            }
        }
        else -> null
    }
