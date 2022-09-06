package de.westnordost.streetcomplete.quests.cycleway

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.osm.cycleway.Cycleway
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.ADVISORY_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.BUSWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.EXCLUSIVE_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE_NO_ONEWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.PICTOGRAMS
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SEPARATE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SIDEWALK_EXPLICIT
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SUGGESTION_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_LANE
import de.westnordost.streetcomplete.view.DrawableImage
import de.westnordost.streetcomplete.view.DrawableWrapper
import de.westnordost.streetcomplete.view.Image
import de.westnordost.streetcomplete.view.ResImage
import de.westnordost.streetcomplete.view.ResText
import de.westnordost.streetcomplete.view.controller.StreetSideItem
import de.westnordost.streetcomplete.view.image_select.Item2

fun Cycleway.asDialogItem(context: Context, isLeftHandTraffic: Boolean, isContraflowInOneway: Boolean) =
    Item2(
        this,
        getDialogIcon(context, isLeftHandTraffic),
        ResText(getTitleResId(isContraflowInOneway))
    )

fun Cycleway.asStreetSideItem(isLeftHandTraffic: Boolean, isContraflowInOneway: Boolean, noEntrySignDrawableResId: Int) =
    StreetSideItem(
        this,
        getIconResId(isLeftHandTraffic),
        getTitleResId(isContraflowInOneway),
        getDialogIconResId(isLeftHandTraffic),
        getFloatingIconResId(isContraflowInOneway, noEntrySignDrawableResId)
    )

private fun Cycleway.getDialogIcon(context: Context, isLeftHandTraffic: Boolean): Image {
    val id = getDialogIconResId(isLeftHandTraffic)
    return if (isLeftHandTraffic) {
        DrawableImage(Rotate180Degrees(context.getDrawable(id)!!))
    } else {
        ResImage(id)
    }
}

private fun Cycleway.getDialogIconResId(isLeftHandTraffic: Boolean): Int =
    when (this) {
        NONE -> R.drawable.ic_cycleway_none_in_selection
        SEPARATE -> R.drawable.ic_cycleway_separate
        else -> getIconResId(isLeftHandTraffic)
    }

private class Rotate180Degrees(drawable: Drawable) : DrawableWrapper(drawable) {
    override fun draw(canvas: Canvas) {
        canvas.scale(-1f, -1f, bounds.width() / 2f, bounds.height() / 2f)
        drawable.bounds = bounds
        drawable.draw(canvas)
    }
}

private fun Cycleway.getFloatingIconResId(isContraflowInOneway: Boolean, noEntrySignDrawableResId: Int): Int? = when (this) {
    NONE -> if (isContraflowInOneway) noEntrySignDrawableResId else null
    SEPARATE -> R.drawable.ic_sidewalk_floating_separate
    else -> null
}

private fun Cycleway.getIconResId(isLeftHandTraffic: Boolean): Int =
    if (isLeftHandTraffic) getLeftHandTrafficIconResId() else getRightHandTrafficIconResId()

private fun Cycleway.getRightHandTrafficIconResId(): Int = when (this) {
    UNSPECIFIED_LANE ->  R.drawable.ic_cycleway_lane
    EXCLUSIVE_LANE ->    R.drawable.ic_cycleway_lane
    ADVISORY_LANE ->     R.drawable.ic_cycleway_shared_lane
    SUGGESTION_LANE ->   R.drawable.ic_cycleway_suggestion_lane
    TRACK ->             R.drawable.ic_cycleway_track
    NONE ->              R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY ->    R.drawable.ic_cycleway_none_no_oneway
    PICTOGRAMS ->        R.drawable.ic_cycleway_pictograms
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit
    DUAL_LANE ->         R.drawable.ic_cycleway_lane_dual
    DUAL_TRACK ->        R.drawable.ic_cycleway_track_dual
    BUSWAY ->            R.drawable.ic_cycleway_bus_lane
    SEPARATE ->          R.drawable.ic_cycleway_none
    else -> 0
}

private fun Cycleway.getLeftHandTrafficIconResId(): Int = when (this) {
    UNSPECIFIED_LANE ->  R.drawable.ic_cycleway_lane_l
    EXCLUSIVE_LANE ->    R.drawable.ic_cycleway_lane_l
    ADVISORY_LANE ->     R.drawable.ic_cycleway_shared_lane_l
    SUGGESTION_LANE ->   R.drawable.ic_cycleway_suggestion_lane
    TRACK ->             R.drawable.ic_cycleway_track_l
    NONE ->              R.drawable.ic_cycleway_none
    NONE_NO_ONEWAY ->    R.drawable.ic_cycleway_none_no_oneway_l
    PICTOGRAMS ->        R.drawable.ic_cycleway_pictograms_l
    SIDEWALK_EXPLICIT -> R.drawable.ic_cycleway_sidewalk_explicit_l
    DUAL_LANE ->         R.drawable.ic_cycleway_lane_dual_l
    DUAL_TRACK ->        R.drawable.ic_cycleway_track_dual_l
    BUSWAY ->            R.drawable.ic_cycleway_bus_lane_l
    SEPARATE ->          R.drawable.ic_cycleway_none
    else -> 0
}

private fun Cycleway.getTitleResId(isContraflowInOneway: Boolean): Int = when (this) {
    UNSPECIFIED_LANE ->  R.string.quest_cycleway_value_lane
    EXCLUSIVE_LANE ->    R.string.quest_cycleway_value_lane
    ADVISORY_LANE ->     R.string.quest_cycleway_value_lane_soft
    SUGGESTION_LANE ->   R.string.quest_cycleway_value_suggestion_lane
    TRACK ->             R.string.quest_cycleway_value_track
    NONE -> {
        if (isContraflowInOneway) R.string.quest_cycleway_value_none_and_oneway
        else                      R.string.quest_cycleway_value_none
    }
    NONE_NO_ONEWAY ->    R.string.quest_cycleway_value_none_but_no_oneway
    PICTOGRAMS ->        R.string.quest_cycleway_value_shared
    SIDEWALK_EXPLICIT -> R.string.quest_cycleway_value_sidewalk
    DUAL_LANE ->         R.string.quest_cycleway_value_lane_dual
    DUAL_TRACK ->        R.string.quest_cycleway_value_track_dual
    BUSWAY ->            R.string.quest_cycleway_value_bus_lane
    SEPARATE ->          R.string.quest_cycleway_value_separate
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
