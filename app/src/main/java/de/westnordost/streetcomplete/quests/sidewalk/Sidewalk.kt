package de.westnordost.streetcomplete.quests.sidewalk

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.quests.sidewalk.Sidewalk.*

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

fun SidewalkSides.applyTo(changes: StringMapChangesBuilder) {
    val sidewalkValue = simpleOsmValue
    if (sidewalkValue != null) {
        changes.add("sidewalk", sidewalkValue)
    } else {
        changes.add("sidewalk:left", left.osmValue)
        changes.add("sidewalk:right", right.osmValue)
    }
}
