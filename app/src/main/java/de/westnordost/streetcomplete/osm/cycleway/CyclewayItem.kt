package de.westnordost.streetcomplete.osm.cycleway

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneMirroredResId
import de.westnordost.streetcomplete.util.ktx.advisoryCycleLaneResId
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneMirroredResId
import de.westnordost.streetcomplete.util.ktx.dualCycleLaneResId
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneMirroredResId
import de.westnordost.streetcomplete.util.ktx.exclusiveCycleLaneResId
import de.westnordost.streetcomplete.util.ktx.noEntrySignDrawableResId
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneMirroredResId
import de.westnordost.streetcomplete.util.ktx.pictogramCycleLaneResId
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.controller.StreetSideItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun CyclewayAndDirection.asDialogItem(
    isRight: Boolean,
    isContraflowInOneway: Boolean,
    context: Context,
    countryInfo: CountryInfo
) =
    Item2(
        this,
        getDialogIcon(context, isRight, countryInfo),
        ResText(getTitleResId(isContraflowInOneway))
    )

fun CyclewayAndDirection.asStreetSideItem(
    isRight: Boolean,
    isContraflowInOneway: Boolean,
    countryInfo: CountryInfo
) =
    StreetSideItem(
        this,
        getIconResId(isRight, countryInfo),
        getTitleResId(isContraflowInOneway),
        getDialogIconResId(isRight, countryInfo),
        cycleway.getFloatingIconResId(isContraflowInOneway, countryInfo.noEntrySignDrawableResId)
    )

private fun CyclewayAndDirection.getDialogIcon(
    context: Context,
    isRight: Boolean,
    countryInfo: CountryInfo
): Image {
    val id = getDialogIconResId(isRight, countryInfo)
    return if (countryInfo.isLeftHandTraffic) {
        DrawableImage(Rotate180Degrees(context.getDrawable(id)!!))
    } else {
        ResImage(id)
    }
}

private fun CyclewayAndDirection.getDialogIconResId(isRight: Boolean, countryInfo: CountryInfo): Int =
    when (cycleway) {
        NONE ->     R.drawable.ic_cycleway_none_in_selection
        SEPARATE -> R.drawable.ic_cycleway_separate
        else ->     getIconResId(isRight, countryInfo)
    }

private class Rotate180Degrees(drawable: Drawable) : DrawableWrapperCompat(drawable) {
    override fun draw(canvas: Canvas) {
        canvas.scale(-1f, -1f, bounds.width() / 2f, bounds.height() / 2f)
        drawable?.bounds = bounds
        drawable?.draw(canvas)
    }
}

private fun Cycleway.getFloatingIconResId(isContraflowInOneway: Boolean, noEntrySignDrawableResId: Int): Int? = when (this) {
    NONE ->     if (isContraflowInOneway) noEntrySignDrawableResId else null
    SEPARATE -> R.drawable.ic_floating_separate
    else ->     null
}

private fun CyclewayAndDirection.getIconResId(isRight: Boolean, countryInfo: CountryInfo): Int = when (direction) {
    BOTH -> cycleway.getDualTrafficIconResId(countryInfo)
    else -> {
        val isForward = (direction == FORWARD)
        val showMirrored = isForward xor isRight
        if (showMirrored) {
            cycleway.getLeftHandTrafficIconResId(countryInfo)
        } else {
            cycleway.getRightHandTrafficIconResId(countryInfo)
        }
    }
}

private fun Cycleway.getDualTrafficIconResId(countryInfo: CountryInfo): Int = when (this) {
    UNSPECIFIED_LANE, EXCLUSIVE_LANE ->
        if (countryInfo.isLeftHandTraffic) {
            countryInfo.dualCycleLaneMirroredResId
        } else {
            countryInfo.dualCycleLaneResId
        }
    TRACK ->
        if (countryInfo.isLeftHandTraffic) {
            R.drawable.ic_cycleway_track_dual_l
        } else {
            R.drawable.ic_cycleway_track_dual
        }
    SIDEWALK_EXPLICIT ->                   R.drawable.ic_cycleway_sidewalk_explicit_dual
    else ->                                0
}

private fun Cycleway.getRightHandTrafficIconResId(countryInfo: CountryInfo): Int = when (this) {
    UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneResId
    EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneResId
    ADVISORY_LANE ->     countryInfo.advisoryCycleLaneResId
    SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneResId
    TRACK ->             R.drawable.ic_cycleway_track
    NONE ->              R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY ->    R.drawable.ic_cycleway_none_no_oneway
    PICTOGRAMS ->        countryInfo.pictogramCycleLaneResId
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit
    BUSWAY ->            R.drawable.ic_cycleway_bus_lane
    SEPARATE ->          R.drawable.ic_cycleway_none
    SHOULDER ->          R.drawable.ic_cycleway_shoulder
    else -> 0
}

private fun Cycleway.getLeftHandTrafficIconResId(countryInfo: CountryInfo): Int = when (this) {
    UNSPECIFIED_LANE ->  countryInfo.exclusiveCycleLaneMirroredResId
    EXCLUSIVE_LANE ->    countryInfo.exclusiveCycleLaneMirroredResId
    ADVISORY_LANE ->     countryInfo.advisoryCycleLaneMirroredResId
    SUGGESTION_LANE ->   countryInfo.advisoryCycleLaneMirroredResId
    TRACK ->             R.drawable.ic_cycleway_track_l
    NONE ->              R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY ->    R.drawable.ic_cycleway_none_no_oneway_l
    PICTOGRAMS ->        countryInfo.pictogramCycleLaneMirroredResId
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit_l
    BUSWAY ->            R.drawable.ic_cycleway_bus_lane_l
    SEPARATE ->          R.drawable.ic_cycleway_none
    SHOULDER ->          R.drawable.ic_cycleway_shoulder
    else -> 0
}

private fun CyclewayAndDirection.getTitleResId(isContraflowInOneway: Boolean): Int = when (cycleway) {
    UNSPECIFIED_LANE, EXCLUSIVE_LANE -> {
        if (direction == BOTH) {
            R.string.quest_cycleway_value_lane_dual
        } else {
            R.string.quest_cycleway_value_lane
        }
    }
    TRACK -> {
        if (direction == BOTH) {
            R.string.quest_cycleway_value_track_dual
        } else {
            R.string.quest_cycleway_value_track
        }
    }
    SIDEWALK_EXPLICIT -> {
        if (direction == BOTH) {
            R.string.quest_cycleway_value_sidewalk_dual2
        } else {
            R.string.quest_cycleway_value_sidewalk2
        }
    }
    NONE -> {
        if (isContraflowInOneway) {
            R.string.quest_cycleway_value_none_and_oneway
        } else {
            R.string.quest_cycleway_value_none
        }
    }
    ADVISORY_LANE,
    SUGGESTION_LANE ->   R.string.quest_cycleway_value_advisory_lane
    NONE_NO_ONEWAY ->    R.string.quest_cycleway_value_none_but_no_oneway
    PICTOGRAMS ->        R.string.quest_cycleway_value_shared
    BUSWAY ->            R.string.quest_cycleway_value_bus_lane
    SEPARATE ->          R.string.quest_cycleway_value_separate
    SHOULDER ->          R.string.quest_cycleway_value_shoulder
    else -> 0
}
