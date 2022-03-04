package de.westnordost.streetcomplete.osm.sidewalk

import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.NO
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.SEPARATE
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk.YES


data class LeftAndRightSidewalk(val left: Sidewalk?, val right: Sidewalk?)

/** Returns on which sides are sidewalks. Returns null if tagging is unknown */
fun createSidewalkSides(tags: Map<String, String>): LeftAndRightSidewalk? {
    if (!tags.keys.containsAny(KNOWN_SIDEWALK_KEYS)) return null

    val sidewalk = createSidewalksDefault(tags)
    if (sidewalk != null) return sidewalk

    // alternative tagging
    val altSidewalk = createSidewalksAlternative(tags)
    if (altSidewalk != null) return altSidewalk

    return null
}

private fun createSidewalksDefault(tags: Map<String, String>): LeftAndRightSidewalk? = when(tags["sidewalk"]) {
    "left" -> LeftAndRightSidewalk(left = YES, right = NO)
    "right" -> LeftAndRightSidewalk(left = NO, right = YES)
    "both" -> LeftAndRightSidewalk(left = YES, right = YES)
    "no", "none" -> LeftAndRightSidewalk(left = NO, right = NO)
    "separate" -> LeftAndRightSidewalk(left = SEPARATE, right = SEPARATE)
    else -> null
}

private fun createSidewalksAlternative(tags: Map<String, String>): LeftAndRightSidewalk? {
    val sidewalkLeft = tags["sidewalk:both"] ?: tags["sidewalk:left"]
    val sidewalkRight = tags["sidewalk:both"] ?: tags["sidewalk:right"]
    return if (sidewalkLeft != null || sidewalkRight != null) {
        LeftAndRightSidewalk(left = createSidewalkSide(sidewalkLeft), right = createSidewalkSide(sidewalkRight))
    } else {
        null
    }
}

private fun createSidewalkSide(tag: String?): Sidewalk? = when(tag) {
    "yes" -> YES
    "no" -> NO
    "separate" -> SEPARATE
    else -> null
}

private val KNOWN_SIDEWALK_KEYS = listOf(
    "sidewalk", "sidewalk:left", "sidewalk:right", "sidewalk:both"
)
