package de.westnordost.streetcomplete.osm.cycleway_separate

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

    // cycleway implies bicycle=designated
    val bicycle = tags["bicycle"] ?: if (tags["highway"] == "cycleway") "designated" else null
    if (bicycle != "designated") return if (bicycle == "yes") SeparateCycleway.ALLOWED else SeparateCycleway.NONE

    val hasSidewalk = createSidewalkSides(tags)?.any { it == Sidewalk.YES } == true || tags["sidewalk"] == "yes"
    if (hasSidewalk) {
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
