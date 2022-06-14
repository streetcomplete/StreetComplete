package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

data class SidewalkSides(val left: Sidewalk, val right: Sidewalk)

enum class Sidewalk {
    YES,
    NO,
    SEPARATE,
    INVALID
}

/** Value for the sidewalk=* key. Returns null for combinations that can't be expressed with the
 *  sidewalk=* key. */
private val SidewalkSides.simpleOsmValue: String? get() = when {
    left == YES && right == YES -> "both"
    left == YES && right == NO ->  "left"
    left == NO && right == YES ->  "right"
    left == NO && right == NO ->   "no"
    left == SEPARATE && right == SEPARATE -> "separate"
    else -> null
}

private val Sidewalk.osmValue: String get() = when (this) {
    YES -> "yes"
    NO -> "no"
    SEPARATE -> "separate"
    else -> {
        throw IllegalStateException("Attempting to tag invalid sidewalk")
    }
}

fun SidewalkSides.applyTo(tags: Tags) {
    val sidewalkValue = simpleOsmValue
    if (sidewalkValue != null) {
        tags["sidewalk"] = sidewalkValue
        // In case of previous incomplete sidewalk tagging
        tags.remove("sidewalk:left")
        tags.remove("sidewalk:right")
        tags.remove("sidewalk:both")
    } else {
        tags["sidewalk:left"] = left.osmValue
        tags["sidewalk:right"] = right.osmValue
        // In case of previous incorrect sidewalk tagging
        tags.remove("sidewalk:both")
        tags.remove("sidewalk")
    }
    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk")) {
        tags.updateCheckDateForKey("sidewalk")
    }
}
