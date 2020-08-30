package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

data class LeftAndRightCycleway(val left: Cycleway?, val right: Cycleway?)

/** Returns the Cycleway values for the left and right side using the given tags */
fun createCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean): LeftAndRightCycleway? {

    val isForwardOneway = tags["oneway"] == "yes"
    val isReversedOneway = tags["oneway"] == "-1"
    val isOneway = isReversedOneway || isForwardOneway
    val isReverseSideRight = isReversedOneway xor isLeftHandTraffic
    // any opposite tagging implies oneway:bicycle = no
    val isAnyOppositeTagging = tags.filterKeys { it in KNOWN_CYCLEWAY_KEYS }.values.any { it.startsWith("opposite") }
    val isOnewayNotForCyclists = isOneway && (tags["oneway:bicycle"] == "no" || isAnyOppositeTagging)

    if (!isOneway && isAnyOppositeTagging) return null

    var left: Cycleway?
    var right: Cycleway?

    /* For oneways, the naked "cycleway"-keys should be interpreted differently:
    *  F.e. a cycleway=lane in a oneway=yes probably means that only in the flow direction, there
    *  is a lane. F.e. cycleway=opposite_lane means that there is a lane in opposite traffic flow
    *  direction.
    *  Whether there is anything each in the other direction, is not defined, so we have to treat
    *  it that way. */
    val cycleway = createCyclewayForSide(tags, null)
    if (isOneway && cycleway != null && cycleway != NONE) {
        val isOpposite = tags["cycleway"]?.startsWith("opposite") == true
        if (isOpposite) {
            if (isReverseSideRight) {
                left = null
                right = cycleway
            }
            else {
                left = cycleway
                right = null
            }
        } else {
            if (isReverseSideRight) {
                left = cycleway
                right = null
            }
            else {
                left = null
                right = cycleway
            }
        }
    } else {
        // first expand cycleway:both etc into cycleway:left + cycleway:right etc
        val expandedTags = expandRelevantSidesTags(tags)
        // then get the values for left and right
        left = createCyclewayForSide(expandedTags, "left")
        right = createCyclewayForSide(expandedTags, "right")
    }

    /* if there is no cycleway in a direction but it is a oneway in the other direction but not
       for cyclists, we have a special selection for that */
    if (isOnewayNotForCyclists) {
        if (left == NONE && !isReverseSideRight) left = NONE_NO_ONEWAY
        if (right == NONE && isReverseSideRight) right = NONE_NO_ONEWAY
    }

    if (left == null && right == null) return null

    return LeftAndRightCycleway(left, right)
}

/** Returns the Cycleway value using the given tags, for the given side (left or right).
 *  Returns null if nothing (understood) is tagged */
private fun createCyclewayForSide(tags: Map<String, String>, side: String?): Cycleway? {
    val sideVal = if (side != null) ":$side" else ""
    val cyclewayKey = "cycleway$sideVal"

    val cycleway = tags[cyclewayKey]
    val cyclewayLane = tags["$cyclewayKey:lane"]

    val isDual = tags["$cyclewayKey:oneway"] == "no"
    val isSegregated = tags["$cyclewayKey:segregated"] != "no"
    val isAllowedOnSidewalk = tags["sidewalk$sideVal:bicycle"] == "yes"

    val result = when(cycleway) {
        "lane", "opposite_lane" -> {
            when (cyclewayLane) {
                "exclusive", "exclusive_lane", "mandatory" -> {
                    if (isDual) DUAL_LANE
                    else        EXCLUSIVE_LANE
                }
                null -> {
                    if (isDual) DUAL_LANE
                    else        UNSPECIFIED_LANE
                }
                "advisory", "advisory_lane", "soft_lane", "dashed" -> ADVISORY_LANE
                else                                               -> UNKNOWN_LANE
            }
        }
        "shared_lane" -> {
            when (cyclewayLane) {
                "advisory", "advisory_lane", "soft_lane", "dashed" -> SUGGESTION_LANE
                "pictogram"                                        -> PICTOGRAMS
                null                                               -> UNSPECIFIED_SHARED_LANE
                else                                               -> UNKNOWN_SHARED_LANE
            }
        }
        "track", "opposite_track" -> {
            when {
                !isSegregated -> SIDEWALK_EXPLICIT
                isDual        -> DUAL_TRACK
                else          -> TRACK
            }
        }
        "no", "none", "opposite" -> NONE
        "share_busway", "opposite_share_busway" -> BUSWAY
        null -> null
        else -> UNKNOWN
    }

    if (result == null || result == NONE) {
        if (isAllowedOnSidewalk) return SIDEWALK_OK
    }

    return result
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    expandSidesTag("cycleway", "", result)
    expandSidesTag("cycleway", "lane", result)
    expandSidesTag("cycleway", "oneway", result)
    expandSidesTag("cycleway", "segregated", result)
    expandSidesTag("sidewalk", "bicycle", result)
    return result
}

/** Expand my_tag:both and my_tag into my_tag:left and my_tag:right etc */
private fun expandSidesTag(keyPrefix: String, keyPostfix: String, tags: MutableMap<String, String>) {
    val pre = keyPrefix
    val post = if (keyPostfix.isEmpty()) "" else ":$keyPostfix"
    val value = tags["$pre:both$post"] ?: tags["$pre$post"]
    if (value != null) {
        if (!tags.containsKey("$pre:left$post")) tags["$pre:left$post"] = value
        if (!tags.containsKey("$pre:right$post")) tags["$pre:right$post"] = value
    }
}

private val KNOWN_CYCLEWAY_KEYS = listOf(
    "cycleway", "cycleway:left", "cycleway:right", "cycleway:both"
)
