package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides

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
    EXCLUSIVE,
    /** This way is a cycleway only, however it has a sidewalk mapped on the same way, like some
     *  sort of tiny road for cyclists only */
    WITH_SIDEWALK
}

/** Returns the situation for a separately mapped cycleway */
fun createSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {

    if (tags["highway"] !in listOf("path", "footway", "bridleway", "cycleway")) return null

    // cycleway implies bicycle=designated
    val bicycle = tags["bicycle"] ?: if (tags["highway"] == "cycleway") "designated" else null
    if (bicycle != "designated") return if (bicycle == "yes") ALLOWED else NONE

    val sidewalks = createSidewalkSides(tags)
    if (sidewalks?.left == Sidewalk.YES || sidewalks?.right == Sidewalk.YES) {
        return WITH_SIDEWALK
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
    if (foot !in yesOrDesignated && horse !in yesOrDesignated) return EXCLUSIVE

    val segregated = tags["segregated"] == "yes"
    return if (segregated) SEGREGATED else NON_SEGREGATED
}
