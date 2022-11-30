package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.isForwardOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.util.ktx.containsAny

/** Returns the Cycleway values for the left and right side using the given tags */
fun createCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean): LeftAndRightCycleway? {
    if (!tags.keys.containsAny(KNOWN_CYCLEWAY_AND_RELATED_KEYS)) return null

    val isForwardOneway = isForwardOneway(tags)
    val isReversedOneway = isReversedOneway(tags)
    val isOneway = isReversedOneway || isForwardOneway
    val isReverseSideRight = isReversedOneway xor isLeftHandTraffic

    // any unambiguous opposite tagging implies oneway:bicycle = no
    val isOpposite = tags["cycleway"]?.startsWith("opposite") == true
    val isOnewayButNotForCyclists = isOneway && isNotOnewayForCyclists(tags, isLeftHandTraffic)

    // opposite tagging implies a oneway. So tagging is not understood if tags seem to contradict each other
    val isAnyOppositeTagging = tags.filterKeys { it in KNOWN_CYCLEWAY_AND_RELATED_KEYS }.values.any { it.startsWith("opposite") }
    if (!isOneway && isAnyOppositeTagging) return null

    var left: Cycleway? = null
    var right: Cycleway? = null

    // first expand cycleway:both etc into cycleway:left + cycleway:right etc
    val expandedTags = expandRelevantSidesTags(tags)

    /* For oneways, the naked "cycleway"-keys should be interpreted differently:
    *  E.g. a cycleway=lane in a oneway=yes probably means that only in the flow direction, there
    *  is a lane. E.g. cycleway=opposite_lane means that there is a lane in opposite traffic flow
    *  direction.
    *  Whether there is anything each in the other direction, is not defined, so we have to treat
    *  it that way. */
    val nakedCycleway = createCyclewayForSide(expandedTags, null, isLeftHandTraffic)
    if (isOneway && nakedCycleway != null && nakedCycleway != NONE) {
        val isRight = if (isOpposite) isReverseSideRight else !isReverseSideRight
        if (isRight) right = nakedCycleway else left = nakedCycleway
    } else {
        left = createCyclewayForSide(expandedTags, false, isLeftHandTraffic)
        right = createCyclewayForSide(expandedTags, true, isLeftHandTraffic)
    }

    val leftDir = createDirectionForSide(expandedTags, false, isLeftHandTraffic)
    val rightDir = createDirectionForSide(expandedTags, true, isLeftHandTraffic)

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

    if (left == null && right == null) return null

    return LeftAndRightCycleway(
        left?.let { CyclewayAndDirection(it, leftDir) },
        right?.let { CyclewayAndDirection(it, rightDir) }
    )
}

/** Returns the Cycleway value using the given tags, for the given side (left or right).
 *  Returns null if nothing (understood) is tagged */
private fun createCyclewayForSide(
    tags: Map<String, String>,
    isRight: Boolean?,
    isLeftHandTraffic: Boolean
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

    val result = when (cycleway) {
        "lane", "opposite_lane" -> {
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
        "track", "opposite_track" -> {
            if (isSegregated) TRACK else SIDEWALK_EXPLICIT
        }
        "separate" -> SEPARATE
        "no", "opposite" -> NONE
        "share_busway", "opposite_share_busway" -> BUSWAY
        "shoulder" -> SHOULDER
        // values known to be invalid, ambiguous or obsolete:
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

    // fall back to bicycle=use_sidepath if set because it implies there is a separate cycleway
    if (result == null) {
        if (isRight != null) {
            val direction = if (isLeftHandTraffic xor isRight) "forward" else "backward"
            if (tags["bicycle:$direction"] == "use_sidepath") return SEPARATE
        }
        if (tags["bicycle"] == "use_sidepath") return SEPARATE
    }
    return result
}

private fun createDirectionForSide(
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
    val defaultDirection =
        if (isRight xor isLeftHandTraffic) Direction.FORWARD else Direction.BACKWARD

    return explicitDirection ?: defaultDirection
}

private fun expandRelevantSidesTags(tags: Map<String, String>): Map<String, String> {
    val result = tags.toMutableMap()
    expandSidesTag("cycleway", "", result)
    expandSidesTag("cycleway", "lane", result)
    expandSidesTag("cycleway", "oneway", result)
    expandSidesTag("cycleway", "segregated", result)
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

private val KNOWN_CYCLEWAY_AND_RELATED_KEYS = listOf(
    "cycleway", "cycleway:left", "cycleway:right", "cycleway:both",
    "bicycle", "bicycle:forward", "bicycle:backward"
)
