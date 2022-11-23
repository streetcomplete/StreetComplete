package de.westnordost.streetcomplete.osm.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway.*
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.expandSides
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.isInContraflowOfOneway
import de.westnordost.streetcomplete.osm.isNotOnewayForCyclists
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
    tags.expandSides("cycleway")
    tags.expandSides("cycleway", "lane")
    tags.expandSides("cycleway", "oneway")
    tags.expandSides("cycleway", "segregated")

    applyOnewayNotForCyclists(tags, isLeftHandTraffic)
    left?.applyTo(tags, false, isLeftHandTraffic)
    right?.applyTo(tags, true, isLeftHandTraffic)

    LeftAndRightSidewalk(
        if (left == SIDEWALK_EXPLICIT) Sidewalk.YES else null,
        if (right == SIDEWALK_EXPLICIT) Sidewalk.YES else null,
    ).applyTo(tags)

    tags.mergeSides("cycleway")
    tags.mergeSides("cycleway", "lane")
    tags.mergeSides("cycleway", "oneway")
    tags.mergeSides("cycleway", "segregated")

    // update check date
    if (!tags.hasChanges || tags.hasCheckDateForKey("cycleway")) {
        tags.updateCheckDateForKey("cycleway")
    }
}

private fun LeftAndRightCycleway.applyOnewayNotForCyclists(tags: Tags, isLeftHandTraffic: Boolean) {
    // TODO:
    /*
     leftDirection ?: tags-leftDirection
     rightDirection ?: tags-rightDirection

     isOnewayNotForCyclists = leftDirection == 0 || rightDirection == 0 || leftDirection != rightDirection


     */

    val isOnewayButNotForCyclists = isOneway(tags) && (
        isNotOnewayForCyclistsNow(tags, isLeftHandTraffic)
        ?: isNotOnewayForCyclists(tags, isLeftHandTraffic)
    )

    // oneway situation for bicycles
    if (isOnewayButNotForCyclists) {
        tags["oneway:bicycle"] = "no"
    } else {
        if (tags["oneway:bicycle"] == "no") {
            tags.remove("oneway:bicycle")
        }
    }
}

private fun Cycleway.applyTo(tags: Tags, isRight: Boolean, isLeftHandTraffic: Boolean) {
    val side = if (isRight) "right" else "left"
    val cyclewayKey = "cycleway:$side"
    when (this) {
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
        EXCLUSIVE_LANE, DUAL_LANE -> {
            tags[cyclewayKey] = "lane"
            tags["$cyclewayKey:lane"] = "exclusive"
        }
        TRACK, DUAL_TRACK -> {
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
        SEPARATE -> {
            tags[cyclewayKey] = "separate"
        }
        else -> {
            throw IllegalArgumentException("Invalid cycleway")
        }
    }

    // no oneway for dual lane etc.
    if (!isOneway) {
        tags["$cyclewayKey:oneway"] = "no"
    } else {
        val isPhysicalCycleway = this != NONE && this != NONE_NO_ONEWAY && this != SEPARATE
        // ... but explicit oneway for lanes going in contraflow of oneways because this is not implied
        if (isInContraflowOfOneway(isRight, tags, isLeftHandTraffic) && isPhysicalCycleway) {
            tags["$cyclewayKey:oneway"] = if (isReversedOneway(tags)) "yes" else "-1"
        } else {
            // otherwise clear
            if (tags["$cyclewayKey:oneway"] == "no") {
                tags.remove("$cyclewayKey:oneway")
            }
        }
    }

    // clear previous cycleway:lane value
    if (!isLane) {
        tags.remove("$cyclewayKey:lane")
    }

    // clear previous cycleway:segregated value
    val touchedSegregatedValue = this in listOf(SIDEWALK_EXPLICIT, TRACK, DUAL_TRACK)
    if (!touchedSegregatedValue) {
        tags.remove("$cyclewayKey:segregated")
    }
}
