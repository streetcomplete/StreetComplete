package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

data class CyclewaySides(val left: Cycleway?, val right: Cycleway?)

/** Returns the Cycleway values for the left and right side using the given tags, first trying to
 *  read cycleway:left/cycleway:right if it is set, otherwise read cycleway:both if it is set and
 *  lastly read cycleway if it is set */
fun createCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean): CyclewaySides {
    val isForwardOneway = tags["oneway"] == "yes"
    val isReversedOneway = tags["oneway"] == "-1"
    val isOneway = isReversedOneway || isForwardOneway
    val isOnewayNotForCyclists = isOneway && tags["oneway:bicycle"] == "no"

    val sides = createCyclewaySidesIgnoreContraflow(tags, isLeftHandTraffic)
    var left = sides.left
    var right = sides.right

    /* if there is no cycleway in a direction but it is a oneway in the other direction but not
       for cyclists, we have a special selection for that */
    if (isOnewayNotForCyclists) {
        val isReverseSideRight = isReversedOneway xor isLeftHandTraffic

        if (left == NONE && !isReverseSideRight) left = NONE_NO_ONEWAY
        if (right == NONE && isReverseSideRight) right = NONE_NO_ONEWAY
    }

    return CyclewaySides(left, right)
}

/** Returns the Cycleway values for the left and right side using the given tags, first trying to
 *  read cycleway:left/cycleway:right if it is set, otherwise read cycleway:both if it is set and
 *  lastly read cycleway if it is set */
private fun createCyclewaySidesIgnoreContraflow(tags: Map<String, String>, isLeftHandTraffic: Boolean): CyclewaySides {
    val left = createCyclewayForSide(tags, Side.LEFT)
    val right = createCyclewayForSide(tags, Side.RIGHT)
    if (left != null || right != null) return CyclewaySides(left, right)

    val both = createCyclewayForSide(tags, Side.BOTH)
    if (both != null) return CyclewaySides(both, both)

    val generic = createCyclewayForSide(tags, null)
    /* For the opposite_* values, the "cycleway" tag has a special meaning:
       f.e. cycleway=opposite_lane means that there is a lane in opposite traffic flow direction
       and nothing in the flow direction. */
    if (generic != null) {
        val isForwardOneway = tags["oneway"] == "yes"
        val isReversedOneway = tags["oneway"] == "-1"
        val isOneway = isReversedOneway || isForwardOneway
        val isReverseSideRight = isReversedOneway xor isLeftHandTraffic
        val isOppositeTagging = tags["cycleway"]?.startsWith("opposite") == true

        if (isOppositeTagging && isOneway) {
            if (isReverseSideRight) return CyclewaySides(NONE, generic)
            else return CyclewaySides(generic, NONE)
        }
    }
    return CyclewaySides(generic, generic)
}

private enum class Side(val value: String) {
    LEFT("left"), RIGHT("right"), BOTH("both")
}

/** Returns the Cycleway value using the given tags, for the given side (left, right or both).
 *  If the side is null, for the generic cycleway=...
 *  Returns null if nothing (understood) is tagged */
private fun createCyclewayForSide(tags: Map<String, String>, side: Side?): Cycleway? {
    val sideVal = if (side == null) "" else ":${side.value}"
    val cyclewayKey = "cycleway$sideVal"

    val cycleway = tags[cyclewayKey]
    val cyclewayLane = tags["$cyclewayKey:lane"]

    // TODO understand (and tag) also cycleway:left=track + cycleway:segregated=no? (without defined side)
    val isDual = tags["$cyclewayKey:oneway"] == "no"
    val isSegregated = tags["$cyclewayKey:segregated"] != "no"
    val isAllowedOnSidewalk = tags["sidewalk$sideVal:bicycle"] == "yes"

    return when(cycleway) {
        "lane", "opposite_lane" -> {
            when (cyclewayLane) {
                "exclusive", "exclusive_lane", "mandatory" -> {
                    if (isDual) DUAL_LANE
                    else        EXCLUSIVE_LANE
                }
                null -> {
                    if (isDual) DUAL_LANE
                    else        LANE_UNSPECIFIED
                }
                "advisory", "advisory_lane", "soft_lane", "dashed" -> ADVISORY_LANE
                else                                               -> null
            }
        }
        "shared_lane" -> {
            when (cyclewayLane) {
                "advisory", "advisory_lane", "soft_lane", "dashed" -> SUGGESTION_LANE
                "pictogram"                                        -> PICTOGRAMS
                else                                               -> null
            }
        }
        "track", "opposite_track" -> {
            when {
                !isSegregated -> SIDEWALK_EXPLICIT
                isDual        -> DUAL_TRACK
                else          -> TRACK
            }
        }
        "no", "none", "opposite" -> {
            when {
                isAllowedOnSidewalk    -> SIDEWALK_OK
                else                   -> NONE
            }
        }
        "share_busway", "opposite_share_busway" -> BUSWAY
        else -> null
    }
}
