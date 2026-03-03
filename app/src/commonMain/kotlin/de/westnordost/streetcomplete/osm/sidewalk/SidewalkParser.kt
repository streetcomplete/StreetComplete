package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.Sides
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.util.ktx.containsAny

/** Returns on which sides are sidewalks. Returns null if there is no sidewalk tagging */
fun parseSidewalkSides(tags: Map<String, String>): Sides<Sidewalk>? {
    if (!tags.keys.containsAny(KNOWN_SIDEWALK_KEYS)) return null

    val sidewalk = parseSidewalksDefault(tags)
    // alternative tagging
    val altSidewalk = parseSidewalksAlternative(tags)

    // has mixture of both sidewalk tagging styles
    if (sidewalk != null && altSidewalk != null) return Sides(INVALID, INVALID)

    // has sidewalk tagging, but not known
    if (sidewalk == null && altSidewalk == null) {
        return Sides(INVALID, INVALID)
    }

    // has valid sidewalk tagging of only one tagging style
    if (sidewalk != null) return sidewalk
    if (altSidewalk != null) return altSidewalk

    return null
}

private fun parseSidewalksDefault(tags: Map<String, String>): Sides<Sidewalk>? = when (tags["sidewalk"]) {
    "left" -> Sides(left = YES, right = NO)
    "right" -> Sides(left = NO, right = YES)
    "both" -> Sides(left = YES, right = YES)
    "no", "none" -> Sides(left = NO, right = NO)
    "separate" -> Sides(left = SEPARATE, right = SEPARATE)
    null -> null
    else -> Sides(left = INVALID, right = INVALID)
}

private fun parseSidewalksAlternative(tags: Map<String, String>): Sides<Sidewalk>? {
    if (tags["sidewalk:both"] != null &&
        (tags["sidewalk:left"] != null || tags["sidewalk:right"] != null)) {
        return Sides(INVALID, INVALID)
    }
    val sidewalkLeft = tags["sidewalk:both"] ?: tags["sidewalk:left"]
    val sidewalkRight = tags["sidewalk:both"] ?: tags["sidewalk:right"]
    return if (sidewalkLeft != null || sidewalkRight != null) {
        Sides(
            left = parseSidewalkSide(sidewalkLeft),
            right = parseSidewalkSide(sidewalkRight)
        )
    } else {
        null
    }
}

private fun parseSidewalkSide(tag: String?): Sidewalk? = when (tag) {
    "yes" -> YES
    "no", "none" -> NO
    "separate" -> SEPARATE
    null -> null
    else -> INVALID
}

val KNOWN_SIDEWALK_KEYS = listOf(
    "sidewalk", "sidewalk:left", "sidewalk:right", "sidewalk:both"
)
