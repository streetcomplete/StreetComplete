package de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.bicycle_in_pedestrian_street.BicycleInPedestrianStreet.*

enum class BicycleInPedestrianStreet {
    /** Pedestrian area also designated for pedestrians (like shared-use path) */
    DESIGNATED,
    /** Bicycles explicitly allowed in pedestrian area */
    ALLOWED,
    /** Bicycles explicitly not allowed in pedestrian area */
    NOT_ALLOWED,
    /** Nothing is signed about bicycles in pedestrian area (probably disallowed, but depends on
     *  legislation */
    NOT_SIGNED
}

fun parseBicycleInPedestrianStreet(tags: Map<String, String>): BicycleInPedestrianStreet? {
    val bicycleSigned = tags["bicycle:signed"] == "yes"
    return when {
        tags["highway"] != "pedestrian" -> null
        tags["bicycle"] == "designated" -> DESIGNATED
        tags["bicycle"] in yesButNotDesignated && bicycleSigned -> ALLOWED
        tags["bicycle"] in noCycling && bicycleSigned -> NOT_ALLOWED
        else -> NOT_SIGNED
    }
}

private val yesButNotDesignated = setOf(
    "yes", "permissive", "private", "destination", "customers", "permit"
)

private val noCycling = setOf(
    "no", "dismount"
)

fun BicycleInPedestrianStreet.applyTo(tags: Tags) {
    // note the implementation is quite similar to that in SeparateCyclewayCreator
    when (this) {
        DESIGNATED -> {
            tags["bicycle"] = "designated"
            // if bicycle:signed is explicitly no, set it to yes
            if (tags["bicycle:signed"] == "no") tags["bicycle:signed"] = "yes"
        }
        ALLOWED -> {
            tags["bicycle"] = "yes"
            tags["bicycle:signed"] = "yes"
        }
        NOT_ALLOWED -> {
            if (tags["bicycle"] !in noCycling) tags["bicycle"] = "no"
            tags["bicycle:signed"] = "yes"
        }
        NOT_SIGNED -> {
            // only remove if designated before, it might still be allowed by legislation!
            if (tags["bicycle"] == "designated") tags.remove("bicycle")
            tags.remove("bicycle:signed")
        }
    }
}
