package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.*
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

data class LeftAndRightSidewalk(val left: Sidewalk?, val right: Sidewalk?)

enum class Sidewalk {
    YES,
    NO,
    SEPARATE,
    INVALID
}

fun LeftAndRightSidewalk.validOrNullValues(): LeftAndRightSidewalk {
    if (left != INVALID && right != INVALID) return this
    return LeftAndRightSidewalk(left?.takeIf { it != INVALID }, right?.takeIf { it != INVALID })
}

fun LeftAndRightSidewalk.applyTo(tags: Tags) {
    val currentSidewalk = createSidewalkSides(tags)

    // was set before and changed: may be incorrect now - remove subtags!
    if (currentSidewalk?.left != null && currentSidewalk.left != left ||
        currentSidewalk?.right != null && currentSidewalk.right != right) {
        val sidewalkSubtagging = Regex("^sidewalk:(left|right|both):.*")
        for (key in tags.keys) {
            if (key.matches(sidewalkSubtagging)) {
                tags.remove(key)
            }
        }
    }

    val sidewalkValue = simpleOsmValue
    if (sidewalkValue != null) {
        tags["sidewalk"] = sidewalkValue
        // In case of previous incomplete sidewalk tagging
        tags.remove("sidewalk:left")
        tags.remove("sidewalk:right")
        tags.remove("sidewalk:both")
    } else {
        if (left != null)  tags["sidewalk:left"] = left.osmValue
        if (right != null) tags["sidewalk:right"] = right.osmValue
        // In case of previous incorrect sidewalk tagging
        tags.remove("sidewalk:both")
        tags.remove("sidewalk")
    }

    if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk")) {
        tags.updateCheckDateForKey("sidewalk")
    }
}

/** Value for the sidewalk=* key. Returns null for combinations that can't be expressed with the
 *  sidewalk=* key. */
private val LeftAndRightSidewalk.simpleOsmValue: String? get() = when {
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
        throw IllegalArgumentException("Attempting to tag invalid sidewalk")
    }
}
