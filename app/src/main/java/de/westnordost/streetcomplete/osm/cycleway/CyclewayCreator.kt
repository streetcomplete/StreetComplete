package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.quests.cycleway.CyclewayAnswer

fun CyclewayAnswer.applyTo(tags: Tags) {
    if (left == null && right == null) return

    clearCycleway(null, tags)
    if (left == right) {
        applyCyclewayTo(left!!.cycleway, Side.BOTH, 0, tags)
        clearCycleway(Side.LEFT, tags)
        clearCycleway(Side.RIGHT, tags)
    } else {
        if (left != null) {
            applyCyclewayTo(left.cycleway, Side.LEFT, left.dirInOneway, tags)
        }
        if (right != null) {
            applyCyclewayTo(right.cycleway, Side.RIGHT, right.dirInOneway, tags)
        }
        clearCycleway(Side.BOTH, tags)
    }

    // oneway situation for bicycles
    if (isOnewayNotForCyclists) {
        tags["oneway:bicycle"] = "no"
    } else {
        if (tags["oneway:bicycle"] == "no") {
            tags.remove("oneway:bicycle")
        }
    }

    // update check date
    if (!tags.hasChanges || tags.hasCheckDateForKey("cycleway")) {
        tags.updateCheckDateForKey("cycleway")
    }
}

private enum class Side(val value: String) {
    LEFT("left"),
    RIGHT("right"),
    BOTH("both")
}

/** clear previous answers for the given side */
private fun clearCycleway(side: Side?, tags: Tags) {
    val sideVal = if (side == null) "" else ":" + side.value
    val cyclewayKey = "cycleway$sideVal"

    // only things are cleared that are set by this quest
    // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
    tags.remove(cyclewayKey)
    tags.remove("$cyclewayKey:lane")
    tags.remove("$cyclewayKey:oneway")
    tags.remove("$cyclewayKey:segregated")
}

private fun applyCyclewayTo(cycleway: Cycleway, side: Side, dir: Int, tags: Tags) {
    val directionValue = when {
        dir > 0 -> "yes"
        dir < 0 -> "-1"
        else -> null
    }

    val cyclewayKey = "cycleway:" + side.value

    when (cycleway) {
        NONE,
        NONE_NO_ONEWAY -> {
            tags[cyclewayKey] = "no"
        }
        EXCLUSIVE_LANE,
        ADVISORY_LANE,
        UNSPECIFIED_LANE -> {
            tags[cyclewayKey] = "lane"
            if (directionValue != null) {
                tags["$cyclewayKey:oneway"] = directionValue
            }
            if (cycleway == EXCLUSIVE_LANE) {
                tags["$cyclewayKey:lane"] = "exclusive"
            } else if (cycleway == ADVISORY_LANE) {
                tags["$cyclewayKey:lane"] = "advisory"
            }
        }
        TRACK -> {
            tags[cyclewayKey] = "track"
            if (directionValue != null) {
                tags["$cyclewayKey:oneway"] = directionValue
            }
            if (tags.containsKey("$cyclewayKey:segregated")) {
                tags["$cyclewayKey:segregated"] = "yes"
            }
        }
        DUAL_TRACK -> {
            tags[cyclewayKey] = "track"
            tags["$cyclewayKey:oneway"] = "no"
        }
        DUAL_LANE -> {
            tags[cyclewayKey] = "lane"
            tags["$cyclewayKey:oneway"] = "no"
            tags["$cyclewayKey:lane"] = "exclusive"
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
        BUSWAY -> {
            tags[cyclewayKey] = "share_busway"
        }
        SEPARATE -> {
            tags[cyclewayKey] = "separate"
        }
        else -> {
            throw IllegalArgumentException("Invalid cycleway")
        }
    }

    // clear previous cycleway:lane value
    if (!cycleway.isLane) {
        tags.remove("$cyclewayKey:lane")
    }
    // clear previous cycleway:oneway=no value (if not about to set a new value)
    if (cycleway.isOneway && directionValue == null) {
        if (tags["$cyclewayKey:oneway"] == "no") {
            tags.remove("$cyclewayKey:oneway")
        }
    }
    // clear previous cycleway:segregated=no value
    if (cycleway != SIDEWALK_EXPLICIT && cycleway != TRACK) {
        if (tags["$cyclewayKey:segregated"] == "no") {
            tags.remove("$cyclewayKey:segregated")
        }
    }
}

