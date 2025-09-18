package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides

/** Returns the situation for a separately mapped cycleway */
fun parseSeparateCycleway(tags: Map<String, String>): SeparateCycleway? {
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

    val bicycleSigned = tags["bicycle:signed"] == "yes"

    // footway implies foot=designated, path implies foot=yes
    val foot = tags["foot"] ?: when (tags["highway"]) {
        "footway" -> "designated"
        "path" -> "yes"
        else -> null // only happens if highway=cycleway
    }

    // invalid tagging: e.g. highway=footway + foot=no
    if ((foot == null || foot in noFoot) && (bicycle == null || bicycle in noCycling)) return null

    if (bicycle in noCycling && bicycleSigned) return NOT_ALLOWED

    if (bicycle in yesButNotDesignated && bicycleSigned && foot == "designated") return ALLOWED_ON_FOOTWAY

    /*
    Not displaying bicycle=yes and bicycle=no on footways and treating it the same because
    whether riding a bike on a footway is allowed by default (without requiring signs) or
    only under certain conditions (e.g. certain minimum width of sidewalk) is very much
    dependent on the country or state one is in.

    Hence, it is not verifiable well for the on-site surveyor: If there is no sign that
    specifically allows or forbids cycling on a footway, the user is left with his loose
    (mis)understanding of the local legislation to decide. After all, bicycle=yes/no
    is (usually) nothing physical, but merely describes what is legal. It is in that sense
    then not information surveyable on-the-ground, unless specifically signed.
    bicycle=yes/no does however not make a statement about from where this info is derived.

    So, from an on-site surveyor point of view, it is always better to record what is signed,
    instead of what follows from that signage.

    Signage, however, is out of scope of this overlay because while the physical presence of
    a cycleway can be validated at a glance, the presence of a sign requires to walk a bit up
    or down the street in order to find (or not find) a sign.
    More importantly, at the time of writing, there is no way to tag the information that a
    bicycle=* access restriction is derived from the presence of a sign. This however is a
    prerequisite for it being  displayed as a selectable option due to the reasons stated
    above.
    */

    if (bicycle in yesButNotDesignated && foot != "designated") return PATH

    if (bicycle != "designated") return NON_DESIGNATED_ON_FOOTWAY
    val hasSidewalk = parseSidewalkSides(tags)?.any { it == Sidewalk.YES } == true || tags["sidewalk"] == "yes"
    if (hasSidewalk) return EXCLUSIVE_WITH_SIDEWALK

    if (tags["segregated"] == "yes") return SEGREGATED

    if (foot != "designated") return EXCLUSIVE

    return NON_SEGREGATED
}

private val noCycling = setOf(
    "no", "dismount"
)

private val noFoot = setOf(
    "no", "use_sidepath"
)

private val yesButNotDesignated = setOf(
    "yes", "permissive", "private", "destination", "customers", "permit"
)
