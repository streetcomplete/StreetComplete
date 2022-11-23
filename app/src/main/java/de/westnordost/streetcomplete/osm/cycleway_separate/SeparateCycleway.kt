package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*

enum class SeparateCycleway {
    /** No (designated) cycleway */
    NONE,
    /** Bicycles allowed on cycleway */
    ALLOWED,
    /** Not segregated from footway or bridleway mapped on same way */
    NON_SEGREGATED,
    /** Segregated from footway or bridleway mapped on same way */
    SEGREGATED,
    /** This way is a cycleway only, no footway or bridleway mapped on the same way */
    EXCLUSIVE
}

/** Returns the situation for a separately mapped cycleway */
fun createSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {
    // TODO what about sidewalk?

    if (tags["highway"] !in listOf("path", "footway", "bridleway", "cycleway")) return null

    // cycleway implies bicycle=designated
    val bicycle = tags["bicycle"] ?: if (tags["highway"] == "cycleway") "designated" else null
    if (bicycle != "designated") return if (bicycle == "yes") ALLOWED else NONE

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
    if (foot !in yesOrDesignated && horse !in yesOrDesignated) return EXCLUSIVE

    val segregated = tags["segregated"] == "yes"
    return if (segregated) SEGREGATED else NON_SEGREGATED
}
