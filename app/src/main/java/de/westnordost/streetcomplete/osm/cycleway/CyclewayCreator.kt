package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.cycleway.Direction.*
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.isInContraflowOfOneway
import de.westnordost.streetcomplete.osm.isOneway
import de.westnordost.streetcomplete.osm.isReversedOneway
import de.westnordost.streetcomplete.osm.mergeSides
import de.westnordost.streetcomplete.osm.sidewalk.LeftAndRightSidewalk
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.applyTo
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightCycleway.applyTo(tags: Tags, isLeftHandTraffic: Boolean) {
    if (left == null && right == null) return
    /* for being able to modify only one side (e.g. `left` is null while `right` is not null),
       the sides conflated in `:both` keys need to be separated first. E.g. `cycleway=no`
       when left side is made `separate` should become
       - cycleway:right=no
       - cycleway:left=separate
       First separating the values and then later conflating them again, if possible, solves this.
     */

    expandBareTags(tags, isLeftHandTraffic)
    tags.expandSides("cycleway", null, false)
    tags.expandSides("cycleway", "lane", false)
    tags.expandSides("cycleway", "oneway", false)
    tags.expandSides("cycleway", "segregated", false)

    applyOnewayNotForCyclists(tags, isLeftHandTraffic)
    left?.applyTo(tags, false, isLeftHandTraffic)
    right?.applyTo(tags, true, isLeftHandTraffic)

    tags.mergeSides("cycleway")
    tags.mergeSides("cycleway", "lane")
    tags.mergeSides("cycleway", "oneway")
    tags.mergeSides("cycleway", "segregated")

    // update check date
    if (!tags.hasChanges || tags.hasCheckDateForKey("cycleway")) {
        tags.updateCheckDateForKey("cycleway")
    }

    // tag sidewalk after setting the check date because we want to primarily set the check date
    // of the cycleway, not of the sidewalk, if there are no changes
    LeftAndRightSidewalk(
        if (left?.cycleway == SIDEWALK_EXPLICIT) Sidewalk.YES else null,
        if (right?.cycleway == SIDEWALK_EXPLICIT) Sidewalk.YES else null,
    ).applyTo(tags)
}

/* bare cycleway tags are interpreted differently for oneways */
private fun expandBareTags(tags: Tags, isLeftHandTraffic: Boolean) {
    val cycleway = tags["cycleway"] ?: return
    // i.e. they are only expanded into one side. Which side depends on country, direction of oneway
    // and whether it is an "opposite" tag value
    val side = if (isOneway(tags)) {
        val isReverseSideRight = isReversedOneway(tags) xor isLeftHandTraffic
        val isOpposite = cycleway.startsWith("opposite")
        if (isOpposite == isReverseSideRight) "right" else "left"
    } else {
        "both"
    }
    if (!tags.containsKey("cycleway:$side")) {
        tags["cycleway:$side"] = cycleway
            .removePrefix("opposite_") // opposite_track -> track etc.
            .replaceFirst("opposite", "no") // opposite -> no
    }
    tags.remove("cycleway")
    tags.expandBareTagIntoSide("cycleway", "lane", side)
    tags.expandBareTagIntoSide("cycleway", "oneway", side)
    tags.expandBareTagIntoSide("cycleway", "segregated", side)
}

private fun Tags.expandBareTagIntoSide(key: String, suffix: String = "", side: String) {
    val post = if (suffix.isEmpty()) "" else ":$suffix"
    val value = get("$key$post") ?: return
    if (!containsKey("$key:$side$post")) set("$key:$side$post", value)
    remove("$key$post")
}

private fun LeftAndRightCycleway.applyOnewayNotForCyclists(tags: Tags, isLeftHandTraffic: Boolean) {
    if (isOneway(tags) && isNotOnewayForCyclistsNow(tags, isLeftHandTraffic)) {
        tags["oneway:bicycle"] = "no"
    } else {
        if (tags["oneway:bicycle"] == "no") {
            tags.remove("oneway:bicycle")
        }
    }
}

private fun CyclewayAndDirection.applyTo(tags: Tags, isRight: Boolean, isLeftHandTraffic: Boolean) {
    val side = if (isRight) "right" else "left"
    val cyclewayKey = "cycleway:$side"
    when (cycleway) {
        NONE, NONE_NO_ONEWAY -> {
            tags[cyclewayKey] = "no"
        }
        UNSPECIFIED_LANE -> {
            tags[cyclewayKey] = "lane"
            // does not remove any cycleway:lane tag because this value is not considered explicit
        }
        ADVISORY_LANE -> {
            tags[cyclewayKey] = "lane"
            tags["$cyclewayKey:lane"] = "advisory"
        }
        EXCLUSIVE_LANE -> {
            tags[cyclewayKey] = "lane"
            tags["$cyclewayKey:lane"] = "exclusive"
        }
        TRACK -> {
            tags[cyclewayKey] = "track"
            // only set if already set, because "yes" is considered the implicit default
            if (tags.containsKey("$cyclewayKey:segregated")) {
                tags["$cyclewayKey:segregated"] = "yes"
            }
        }
        SIDEWALK_EXPLICIT -> {
            // https://wiki.openstreetmap.org/wiki/File:Z240GemeinsamerGehundRadweg.jpeg
            tags[cyclewayKey] = "track"
            tags["$cyclewayKey:segregated"] = "no"
        }
        PICTOGRAMS -> {
            tags[cyclewayKey] = "shared_lane"
            tags["$cyclewayKey:lane"] = "pictogram"
        }
        SUGGESTION_LANE -> {
            tags[cyclewayKey] = "shared_lane"
            tags["$cyclewayKey:lane"] = "advisory"
        }
        UNSPECIFIED_SHARED_LANE -> {
            tags[cyclewayKey] = "shared_lane"
            // does not remove any cycleway:lane tag because value is not considered explicit
        }
        BUSWAY -> {
            tags[cyclewayKey] = "share_busway"
        }
        SHOULDER -> {
            tags[cyclewayKey] = "shoulder"
        }
        SEPARATE -> {
            tags[cyclewayKey] = "separate"
        }
        else -> {
            throw IllegalArgumentException("Invalid cycleway")
        }
    }

    val isPhysicalCycleway = cycleway != NONE && cycleway != SEPARATE && cycleway != NONE_NO_ONEWAY
    if (!isPhysicalCycleway) {
        tags.remove("$cyclewayKey:oneway")
    } else {
        /* explicitly tag cycleway direction when either
           - the selected direction is not the one assumed by default when the key is missing
           - or the road is a oneway and the cycleway is on the contra-flow side
           - or it is already tagged (to correct it if need be)
         */
        val defaultDirection = Direction.getDefault(isRight, isLeftHandTraffic)
        val isDefaultDirection = defaultDirection == direction
        val isInContraflowOfOneway = isInContraflowOfOneway(tags, direction)
        if (!isDefaultDirection || isInContraflowOfOneway || tags.containsKey("$cyclewayKey:oneway")) {
            tags["$cyclewayKey:oneway"] = direction.onewayValue
        }
    }

    // clear previous cycleway:lane value
    if (!cycleway.isLane) {
        tags.remove("$cyclewayKey:lane")
    }

    // clear previous cycleway:segregated value
    val touchedSegregatedValue = cycleway in listOf(SIDEWALK_EXPLICIT, TRACK)
    if (!touchedSegregatedValue) {
        tags.remove("$cyclewayKey:segregated")
    }
}

private val Direction.onewayValue get() = when (this) {
    FORWARD -> "yes"
    BACKWARD -> "-1"
    BOTH -> "no"
}
