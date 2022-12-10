package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.createSidewalkSides

/** Returns the situation for a separately mapped cycleway */
fun createSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {

    /* bridleways (with cycleways) are not supported, because the complexity explodes when a path
       can not only have have states ranging between two tags with values "no, yes, designated,
       segregated from each other, with sidewalk" but three, with any mixture between these
       possible.
       In particular, it is not clear what "segregated" refers to on a path that potentially allows
       access to all three modes of travel and when re-tagging e.g. an exclusive cycleway to a
       non-designated one, it is unclear whether foot=yes or horse=yes or both should be added. So,
       this data model can only be about the relation between foot & bicycle, not about all three.
     */
    if (tags["highway"] !in listOf("path", "footway", "cycleway")) return null

    // cycleway implies bicycle=designated, path implies bicycle=yes
    val bicycle = tags["bicycle"] ?: when (tags["highway"]) {
        "cycleway" -> "designated"
        "path" -> "yes"
        else -> null // only happens if highway=footway
    }

    // footway implies foot=designated, path implies foot=yes
    val foot = tags["foot"] ?: when (tags["highway"]) {
        "footway" -> "designated"
        "path" -> "yes"
        else -> null // only happens if highway=cycleway
    }

    if (bicycle in noCycling) return NOT_ALLOWED

    if (bicycle in yesButNotDesignated && foot == "designated") return ALLOWED_ON_FOOTWAY

    if (bicycle in yesButNotDesignated && foot in yesButNotDesignated) return PATH

    if (bicycle != "designated") return NON_DESIGNATED

    val hasSidewalk = createSidewalkSides(tags)?.any { it == Sidewalk.YES } == true || tags["sidewalk"] == "yes"
    if (hasSidewalk) return EXCLUSIVE_WITH_SIDEWALK

    if (foot == "no" || foot == null) return EXCLUSIVE

    val segregated = tags["segregated"] == "yes"
    return if (segregated) SEGREGATED else NON_SEGREGATED
}

private val noCycling = setOf(
    "no", "dismount"
)

private val yesButNotDesignated = setOf(
    "yes", "permissive", "private", "destination", "customers", "permit"
)
