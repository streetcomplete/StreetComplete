package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.INVALID
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES
import de.westnordost.streetcomplete.util.ktx.containsAny

/** Returns on which sides are sidewalks. Returns null if there is no sidewalk tagging */
fun createSidewalkSides(tags: Map<String, String>): LeftAndRightSidewalk? {
    if (!tags.keys.containsAny(KNOWN_SIDEWALK_KEYS)) return null

    val sidewalk = createSidewalksDefault(tags)
    // alternative tagging
    val altSidewalk = createSidewalksAlternative(tags)

    // has mixture of both sidewalk tagging styles
    if (sidewalk != null && altSidewalk != null) return LeftAndRightSidewalk(INVALID, INVALID)

    // has sidewalk tagging, but not known
    if (sidewalk == null && altSidewalk == null) {
        return LeftAndRightSidewalk(INVALID, INVALID)
    }

    // has valid sidewalk tagging of only one tagging style
    if (sidewalk != null) return sidewalk
    if (altSidewalk != null) return altSidewalk

    return null
}

private fun createSidewalksDefault(tags: Map<String, String>): LeftAndRightSidewalk? = when (tags["sidewalk"]) {
    "left" -> LeftAndRightSidewalk(left = YES, right = NO)
    "right" -> LeftAndRightSidewalk(left = NO, right = YES)
    "both" -> LeftAndRightSidewalk(left = YES, right = YES)
    "no", "none" -> LeftAndRightSidewalk(left = NO, right = NO)
    "separate" -> LeftAndRightSidewalk(left = SEPARATE, right = SEPARATE)
    null -> null
    else -> LeftAndRightSidewalk(left = INVALID, right = INVALID)
}

private fun createSidewalksAlternative(tags: Map<String, String>): LeftAndRightSidewalk? {
    if (tags["sidewalk:both"] != null &&
        (tags["sidewalk:left"] != null || tags["sidewalk:right"] != null)) {
        return LeftAndRightSidewalk(INVALID, INVALID)
    }
    val sidewalkLeft = tags["sidewalk:both"] ?: tags["sidewalk:left"]
    val sidewalkRight = tags["sidewalk:both"] ?: tags["sidewalk:right"]
    return if (sidewalkLeft != null || sidewalkRight != null) {
        LeftAndRightSidewalk(
            left = createSidewalkSide(sidewalkLeft),
            right = createSidewalkSide(sidewalkRight)
        )
    } else {
        null
    }
}

private fun createSidewalkSide(tag: String?): Sidewalk? = when (tag) {
    "yes" -> YES
    "no", "none" -> NO
    "separate" -> SEPARATE
    null -> null
    else -> INVALID
}

val KNOWN_SIDEWALK_KEYS = listOf(
    "sidewalk", "sidewalk:left", "sidewalk:right", "sidewalk:both"
)
