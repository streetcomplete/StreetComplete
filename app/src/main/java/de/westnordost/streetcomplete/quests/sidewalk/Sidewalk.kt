package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.quests.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.quests.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.quests.sidewalk.Sidewalk.YES

data class SidewalkSides(val left: Sidewalk, val right: Sidewalk)

enum class Sidewalk(val osmValue: String) {
    YES("yes"),
    NO("no"),
    SEPARATE("separate")
}

/** Value for the sidewalk=* key. Returns null for combinations that can't be expressed with the
 *  sidewalk=* key. */
val SidewalkSides.simpleOsmValue: String? get() = when {
    left == YES && right == YES -> "both"
    left == YES && right == NO ->  "left"
    left == NO && right == YES ->  "right"
    left == NO && right == NO ->   "no"
    left == SEPARATE && right == SEPARATE -> "separate"
    else -> null
}

fun SidewalkSides.applyTo(tags: Tags) {
    val sidewalkValue = simpleOsmValue
    if (sidewalkValue != null) {
        tags["sidewalk"] = sidewalkValue
    } else {
        tags["sidewalk:left"] = left.osmValue
        tags["sidewalk:right"] = right.osmValue
    }
}
