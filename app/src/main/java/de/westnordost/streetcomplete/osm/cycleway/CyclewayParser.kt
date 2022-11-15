package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.ADVISORY_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.BUSWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.DUAL_TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.EXCLUSIVE_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.INVALID
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.NONE_NO_ONEWAY
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.PICTOGRAMS
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SEPARATE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SIDEWALK_EXPLICIT
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.SUGGESTION_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.TRACK
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNKNOWN
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNKNOWN_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNKNOWN_SHARED_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_LANE
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.UNSPECIFIED_SHARED_LANE
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.util.ktx.containsAny

/** Returns the Cycleway values for the left and right side using the given tags */
fun createCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean): LeftAndRightCycleway? {
    if (!tags.keys.containsAny(KNOWN_CYCLEWAY_KEYS)) return null

    val isForwardOneway = isForwardOneway(tags)
    val isReversedOneway = isReversedOneway(tags)
    val isOneway = isReversedOneway || isForwardOneway
    val isReverseSideRight = isReversedOneway xor isLeftHandTraffic

    // any unambiguous opposite tagging implies oneway:bicycle = no
    val isOpposite = tags["cycleway"]?.startsWith("opposite") == true
    val isOnewayButNotForCyclists = isOneway && isNotOnewayForCyclists(tags, isLeftHandTraffic)

    // opposite tagging implies a oneway. So tagging is not understood if tags seem to contradict each other
    val isAnyOppositeTagging = tags.filterKeys { it in KNOWN_CYCLEWAY_KEYS }.values.any { it.startsWith("opposite") }
    if (!isOneway && isAnyOppositeTagging) return null

    var left: Cycleway?
    var right: Cycleway?

    /* For oneways, the naked "cycleway"-keys should be interpreted differently:
    *  E.g. a cycleway=lane in a oneway=yes probably means that only in the flow direction, there
    *  is a lane. E.g. cycleway=opposite_lane means that there is a lane in opposite traffic flow
    *  direction.
    *  Whether there is anything each in the other direction, is not defined, so we have to treat
    *  it that way. */
    val cycleway = createCyclewayForSide(tags, null, isReverseSideRight)
    if (isOneway && cycleway != null && cycleway != NONE) {
        if (isOpposite) {
            if (isReverseSideRight) {
                left = null
                right = cycleway
            } else {
                left = cycleway
                right = null
            }
        } else {
            if (isReverseSideRight) {
                left = cycleway
                right = null
            } else {
                left = null
                right = cycleway
            }
        }
    } else {
        // first expand cycleway:both etc into cycleway:left + cycleway:right etc
        val expandedTags = expandRelevantSidesTags(tags)
        // then get the values for left and right
        left = createCyclewayForSide(expandedTags, "left", isReverseSideRight)
        right = createCyclewayForSide(expandedTags, "right", isReverseSideRight)
    }

    /* if there is no cycleway in a direction but it is a oneway in the other direction but not
       for cyclists, we have a special selection for that */
    if (isOnewayButNotForCyclists) {
        if ((left == NONE || left == null) && !isReverseSideRight) left = NONE_NO_ONEWAY
        if ((right == NONE || right == null) && isReverseSideRight) right = NONE_NO_ONEWAY
    }

    if (left == null && right == null) return null

    return LeftAndRightCycleway(left, right)
}

/** Returns the Cycleway value using the given tags, for the given side (left or right).
 *  Returns null if nothing (understood) is tagged */
private fun createCyclewayForSide(
    tags: Map<String, String>,
    side: String?,
    isReverseSideRight: Boolean
): Cycleway? {

    val sideVal = if (side != null) ":$side" else ""
    val cyclewayKey = "cycleway$sideVal"

    val oneway = tags["$cyclewayKey:oneway"]

    val isOnewayInUnexpectedDirection = when (side) {
        "left" -> when (oneway) {
            "yes" -> !isReverseSideRight
            "-1" -> isReverseSideRight
            else -> false
        }
        "right" -> when (oneway) {
            "yes" -> isReverseSideRight
            "-1" -> !isReverseSideRight
            else -> false
        }
        else -> false
    }
    if (isOnewayInUnexpectedDirection) return UNKNOWN

    val cycleway = tags[cyclewayKey]
    val isDual = oneway == "no"
    val cyclewayLane = tags["$cyclewayKey:lane"]
    val isSegregated = tags["$cyclewayKey:segregated"] != "no"

    val result = when (cycleway) {
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
        "separate" -> SEPARATE
        "no", "none", "opposite" -> NONE
        "share_busway", "opposite_share_busway" -> BUSWAY
        "yes", "right", "left", "both", "shared" -> INVALID
        null -> null
        else -> UNKNOWN
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
