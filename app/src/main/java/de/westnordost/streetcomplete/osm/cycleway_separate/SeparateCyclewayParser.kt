package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides
import de.westnordost.streetcomplete.osm.sidewalk.hasSidewalk

/** Returns the situation for a separately mapped cycleway */
fun createSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {

    if (tags["highway"] !in listOf("path", "footway", "bridleway", "cycleway")) return null

    // cycleway implies bicycle=designated
    val bicycle = tags["bicycle"] ?: if (tags["highway"] == "cycleway") "designated" else null
    if (bicycle != "designated") return if (bicycle == "yes") SeparateCycleway.ALLOWED else SeparateCycleway.NONE

    if (createSidewalkSides(tags).hasSidewalk() || tags["sidewalk"] == "yes") {
        return SeparateCycleway.EXCLUSIVE_WITH_SIDEWALK
    }

    // footway implies foot=designated, path implies foot=yes
    val foot = tags["foot"] ?: when (tags["highway"]) {
        "footway" -> "designated"
        "path" -> "yes"
        else -> null
    }
    val horse = tags["horse"] ?: when (tags["highway"]) {
        "bridleway" -> "designated"
        "path" -> "yes"
        else -> null
    }
    val yesOrDesignated = listOf("yes", "designated")
    if (foot !in yesOrDesignated && horse !in yesOrDesignated) return SeparateCycleway.EXCLUSIVE

    val segregated = tags["segregated"] == "yes"
    return if (segregated) SeparateCycleway.SEGREGATED else SeparateCycleway.NON_SEGREGATED
}
