package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides

/** Returns the situation for a separately mapped cycleway */
fun createSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {

    if (tags["highway"] !in listOf("path", "footway", "cycleway")) return null

    // cycleway implies bicycle=designated
    val bicycle = tags["bicycle"] ?: if (tags["highway"] == "cycleway") "designated" else null
    if (bicycle != "designated") return if (bicycle == "yes") SeparateCycleway.ALLOWED else SeparateCycleway.NONE

    val hasSidewalk = createSidewalkSides(tags)?.any { it == Sidewalk.YES || it == Sidewalk.SEPARATE } == true
    if (hasSidewalk || tags["sidewalk"] == "yes") {
        return SeparateCycleway.EXCLUSIVE_WITH_SIDEWALK
    }

    // footway implies foot=designated, path implies foot=yes
    val foot = tags["foot"] ?: when (tags["highway"]) {
        "footway" -> "designated"
        "path" -> "yes"
        else -> null
    }
    if (foot !in listOf("yes", "designated")) return SeparateCycleway.EXCLUSIVE

    val segregated = tags["segregated"] == "yes"
    return if (segregated) SeparateCycleway.SEGREGATED else SeparateCycleway.NON_SEGREGATED
}
