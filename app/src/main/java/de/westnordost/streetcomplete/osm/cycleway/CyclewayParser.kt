package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.expandSidesTags
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.util.ktx.containsAny

/** Returns the Cycleway values for the left and right side using the given tags */
fun parseCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean): LeftAndRightCycleway? {
    if (!tags.keys.containsAny(KNOWN_CYCLEWAY_AND_RELATED_KEYS)) return null

    val isForwardOneway = isForwardOneway(tags)
    val isReversedOneway = isReversedOneway(tags)
    val isOneway = isReversedOneway || isForwardOneway
    val isReverseSideRight = isReversedOneway xor isLeftHandTraffic
    val isOpposite = tags["cycleway"]?.startsWith("opposite") == true
    val isOnewayButNotForCyclists = isOneway && isNotOnewayForCyclists(tags, isLeftHandTraffic)

    var left: Cycleway? = null
    var right: Cycleway? = null

    // first expand cycleway:both etc into cycleway:left + cycleway:right etc
    val expandedTags = expandRelevantSidesTags(tags)

    /* For oneways, the naked "cycleway"-keys should be interpreted differently:
     * E.g. a cycleway=lane in a oneway=yes probably means that only in the flow direction, there
     * is a lane. E.g. cycleway=opposite_lane means that there is a lane in opposite traffic flow
     * direction.
     * Whether there is anything each in the other direction, is not defined, so we have to treat
     * it that way. */
    val nakedCycleway = parseCyclewayForSide(expandedTags, null)
    if (isOneway && nakedCycleway != null && nakedCycleway != NONE) {
        if (isOpposite == isReverseSideRight) {
            right = nakedCycleway
        } else {
            left = nakedCycleway
        }
    } else {
        left = parseCyclewayForSide(expandedTags, false)
        right = parseCyclewayForSide(expandedTags, true)
    }

    val leftDir = parseDirectionForSide(expandedTags, false, isLeftHandTraffic)
    val rightDir = parseDirectionForSide(expandedTags, true, isLeftHandTraffic)

    /* if there is no cycleway in a direction but it is a oneway in the other direction but not
       for cyclists, we have a special selection for that */
    if (isOnewayButNotForCyclists) {
        if ((left == NONE || left == null) && !isReverseSideRight && rightDir != Direction.BOTH) {
            left = NONE_NO_ONEWAY
        }
        if ((right == NONE || right == null) && isReverseSideRight && leftDir != Direction.BOTH) {
            right = NONE_NO_ONEWAY
        }
    }

    // use fallback only if no side is defined
    if (left == null && right == null) {
        left = parseCyclewayForSideFallback(tags, false, isLeftHandTraffic)
        right = parseCyclewayForSideFallback(tags, true, isLeftHandTraffic)
    }

    if (left == null && right == null) {
        return null
    }

    return LeftAndRightCycleway(
        left?.let { CyclewayAndDirection(it, leftDir) },
        right?.let { CyclewayAndDirection(it, rightDir) }
    )
}

/** Returns the Cycleway value using the given tags, for the given side.
 *  Returns null if nothing (understood) is tagged */
private fun parseCyclewayForSide(
    tags: Map<String, String>,
    isRight: Boolean?
): Cycleway? {
    val sideVal = when (isRight) {
        true -> ":right"
        false -> ":left"
        null -> ""
    }
    val cyclewayKey = "cycleway$sideVal"

    val cycleway = tags[cyclewayKey]
    val cyclewayLane = tags["$cyclewayKey:lane"]
    val isSegregated = tags["$cyclewayKey:segregated"] != "no"
    val isCyclingOkOnSidewalk = tags["sidewalk$sideVal:bicycle"] == "yes" && tags["sidewalk$sideVal:bicycle:signed"] == "yes"
    val isCyclingDesignatedOnSidewalk = tags["sidewalk$sideVal:bicycle"] == "designated"

    val result = when (cycleway) {
        "lane" -> {
            when (cyclewayLane) {
                "exclusive" -> EXCLUSIVE_LANE
                null ->        UNSPECIFIED_LANE
                "advisory" ->  ADVISORY_LANE
                "yes", "right", "left", "both", "shoulder", "soft_lane", "mandatory",
                "advisory_lane", "exclusive_lane" -> INVALID
                "pictogram" -> INVALID
                else        -> UNKNOWN_LANE
            }
        }
        "shared_lane" -> {
            when (cyclewayLane) {
                "advisory"  -> SUGGESTION_LANE
                "pictogram" -> PICTOGRAMS
                null        -> UNSPECIFIED_SHARED_LANE
                "yes", "right", "left", "both", "shoulder", "soft_lane", "mandatory",
                "advisory_lane", "exclusive_lane" -> INVALID
                "exclusive" -> INVALID
                else        -> UNKNOWN_SHARED_LANE
            }
        }
        "track" -> {
            if (isSegregated) TRACK else SIDEWALK_EXPLICIT
        }
        "separate" -> SEPARATE
        "no" -> when {
            isCyclingOkOnSidewalk -> SIDEWALK_OK
            isCyclingDesignatedOnSidewalk -> SIDEWALK_EXPLICIT
            else -> NONE
        }
        "share_busway" -> BUSWAY
        "shoulder" -> SHOULDER
        // values known to be invalid, ambiguous or obsolete:
        // deprecated opposite_* tags
        "opposite_lane", "opposite_track", "opposite", "opposite_share_busway",
        // 1.2% - ambiguous: there are more precise tags
        "yes", "right", "left", "both",
        "on_street", "segregated", "shared", // segregated from, shared with what?
        "sidewalk", "share_sidewalk", // allowed on sidewalk or mandatory on sidewalk?
        "unmarked_lane", // I don't even...
        // 0.4% - invalid: maybe synonymous(?) to valid tag or tag combination but never documented
        "none", // no
        "sidepath", "use_sidepath", // separate?
        "buffered_lane", "buffered", "soft_lane", "doorzone", // lane + subtags?
        // 0.1% - troll tags
        "proposed", "construction",
            -> INVALID
        null -> null
        else -> UNKNOWN
    }

    return result
}

private fun parseDirectionForSide(
    tags: Map<String, String>,
    isRight: Boolean,
    isLeftHandTraffic: Boolean
): Direction {
    val sideVal = if (isRight) ":right" else ":left"
    val cyclewayKey = "cycleway$sideVal"
    val explicitDirection = when (tags["$cyclewayKey:oneway"]) {
        "yes" -> Direction.FORWARD
        "-1" ->  Direction.BACKWARD
        "no" ->  Direction.BOTH
        else ->  null
    }
    return explicitDirection ?: Direction.getDefault(isRight, isLeftHandTraffic)
}

/** Returns the cycleway value using the given tags for the given side using other tags that imply
 *  that a cycleway may be there (or not there) */
private fun parseCyclewayForSideFallback(
    tags: Map<String, String>,
    isRight: Boolean?,
    isLeftHandTraffic: Boolean
): Cycleway? {
    // fall back to bicycle=use_sidepath if set because it implies there is a separate cycleway
    if (isRight != null) {
        val direction = if (isLeftHandTraffic xor isRight) "forward" else "backward"
        if (tags["bicycle:$direction"] == "use_sidepath") return SEPARATE
    }
    if (tags["bicycle"] == "use_sidepath") return SEPARATE

    return null
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    result.expandSidesTags("cycleway", "", true)
    result.expandSidesTags("cycleway", "lane", true)
    result.expandSidesTags("cycleway", "oneway", true)
    result.expandSidesTags("cycleway", "segregated", true)
    result.expandSidesTags("sidewalk", "bicycle", true)
    result.expandSidesTags("sidewalk", "bicycle:signed", true)
    return result
}

private val KNOWN_CYCLEWAY_AND_RELATED_KEYS = listOf(
    "cycleway", "cycleway:left", "cycleway:right", "cycleway:both",
    "bicycle", "bicycle:forward", "bicycle:backward"
)
