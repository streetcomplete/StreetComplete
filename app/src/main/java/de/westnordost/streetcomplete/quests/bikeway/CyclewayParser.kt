package de.westnordost.streetcomplete.quests.bikeway

import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.quests.bikeway.Cycleway.*

data class LeftAndRightCycleway(val left: Cycleway?, val right: Cycleway?)

/** Returns the Cycleway values for the left and right side using the given tags */
fun createCyclewaySides(tags: Map<String, String>, isLeftHandTraffic: Boolean, countryCode: String): LeftAndRightCycleway? {

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

    /* The opposite_* values have a special meaning that concern both sides differently:
       f.e. cycleway=opposite_lane means that there is a lane in opposite traffic flow direction
       AND nothing in the flow direction. */
    if (tags["cycleway"]?.startsWith("opposite") == true) {
        /* Thus, there is the potential that this value will conflict with any cycleway:left/right
           tagging, so not both taggings may be analyzed in parallel */
        if (tags.keys.containsAny(listOf("cycleway:left", "cycleway:right" ,"cycleway:both"))) {
            return null
        }

        val oppositeCycleway = createCyclewayForSide(tags, null)

        if (isReverseSideRight) {
            left = NONE
            right = oppositeCycleway
        }
        else {
            left = oppositeCycleway
            right = NONE
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

    /* suggestion lanes are only known in Belgium and Netherlands */
    if (left == SUGGESTION_LANE || right == SUGGESTION_LANE) {
        if (countryCode !in listOf("NL", "BE")) return null
    }
    /* unspecified lanes are only ok in Belgium (no distinction made, all lanes are dashed) */
    if (left == LANE_UNSPECIFIED || right == LANE_UNSPECIFIED) {
        if (countryCode != "BE") return null
    }

    return LeftAndRightCycleway(left, right)
}

/** Returns the Cycleway value using the given tags, for the given side (left or right).
 *  Returns null if nothing (understood) is tagged */
private fun createCyclewayForSide(tags: Map<String, String>, side: String?): Cycleway? {
    val sideVal = if (side != null) ":$side" else ""
    val cyclewayKey = "cycleway$sideVal"

    val cycleway = tags[cyclewayKey]
    val cyclewayLane = tags["$cyclewayKey:lane"]

    val isForwardOneway = tags["oneway"] == "yes"
    val isReversedOneway = tags["oneway"] == "-1"
    val isOneway = isReversedOneway || isForwardOneway
    val isOppositeTagging = cycleway?.startsWith("opposite") == true
    // oneway is required for opposite_* taggings
    if (isOppositeTagging && !isOneway) return null

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
        "no", "none" -> NONE
        "opposite" -> {
            // opposite value is ambiguous/possibly incorrect for cycleway:*, so better reject it
            if (side != null) null
            else              NONE
        }
        "share_busway", "opposite_share_busway" -> BUSWAY
        else -> null
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
