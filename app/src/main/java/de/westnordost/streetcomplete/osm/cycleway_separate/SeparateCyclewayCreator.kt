package de.westnordost.streetcomplete.osm.cycleway_separate

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.cycleway_separate.SeparateCycleway.*
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.sidewalk.KNOWN_SIDEWALK_KEYS
import de.westnordost.streetcomplete.osm.sidewalk.Sidewalk
import de.westnordost.streetcomplete.osm.sidewalk.any
import de.westnordost.streetcomplete.osm.sidewalk.parseSidewalkSides
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun SeparateCycleway.applyTo(tags: Tags) {
    val isCycleway = tags["highway"] == "cycleway"

    // tag bicycle=*, foot=* and retag highway=* if necessary
    when (this) {
        PATH -> {
            tags["highway"] = "path"

            if (tags.containsKey("bicycle") && (tags["bicycle"] == "yes" || tags["bicycle"] == "no")) {
                tags.remove("bicycle")
            }
            // only re-tag to "yes" if defined and not some kind of "yes" value
            if (tags.containsKey("foot") && tags["foot"] !in yesButNotDesignated) {
                tags["foot"] = "yes"
            }
            if (tags.containsKey("bicycle") && tags["bicycle"] !in yesButNotDesignated) {
                tags["bicycle"] = "yes"
            }
            if (tags.containsKey("bicycle:signed")) {
                tags.remove("bicycle:signed")
            }
        }
        NOT_ALLOWED, ALLOWED_ON_FOOTWAY, NON_DESIGNATED -> {
            // not a cycleway if not designated as one!
            if (isCycleway) {
                tags["highway"] = if (tags["foot"] == "designated") "footway" else "path"
            }

            when (this) {
                NOT_ALLOWED -> {
                    if (tags["bicycle"] !in noCycling) tags["bicycle"] = "no"
                }
                ALLOWED_ON_FOOTWAY -> {
                    if (tags["bicycle"] !in yesButNotDesignated) {
                        tags["bicycle"] = "yes"
                        tags["bicycle:signed"] = "yes"
                        // ALLOWED_ON_FOOTWAY implies foot=designated
                        tags["foot"] = "designated"
                    }
                }
                NON_DESIGNATED -> {
                    // if the user answers NOT_DESIGNATED, remove that there is a sign for bicycles but do not touch bicycle=, unless it is designated
                    if (tags.containsKey("bicycle:signed")) tags.remove("bicycle:signed")
                    if (tags["bicycle"] == "designated") tags.remove("bicycle")
                }
                else -> throw IllegalStateException("Unexpected value: $this")
            }
            if (tags["foot"] == "no") {
                tags["foot"] = "yes"
            }
        }
        NON_SEGREGATED, SEGREGATED -> {
            if (!isCycleway || tags.containsKey("bicycle")) {
                tags["bicycle"] = "designated"
            }
            // do not retag highway=cycleway + foot=yes
            if ((isCycleway || tags.containsKey("foot")) && tags["foot"] !in yesButNotDesignated) {
                tags["foot"] = "designated"
            }
        }
        EXCLUSIVE, EXCLUSIVE_WITH_SIDEWALK -> {
            // retag if it is an exclusive cycleway
            tags["highway"] = "cycleway"
            if (tags.containsKey("bicycle")) tags["bicycle"] = "designated"

            if (this == EXCLUSIVE) {
                tags["foot"] = "no"
            } else {
                // follow the same pattern as for roads here: It is uncommon for roads to have foot
                // tagged at all when such roads have sidewalks
                tags.remove("foot")
            }
        }
    }

    // tag or remove sidewalk
    val hasSidewalk = parseSidewalkSides(tags)?.any { it == Sidewalk.YES } == true || tags["sidewalk"] == "yes"
    when (this) {
        EXCLUSIVE_WITH_SIDEWALK -> {
            if (!hasSidewalk) {
                KNOWN_SIDEWALK_KEYS.forEach { tags.remove(it) }
                tags["sidewalk"] = "yes" // cannot be more specific here
            }
        }
        else -> {
            if (hasSidewalk) {
                KNOWN_SIDEWALK_KEYS.forEach { tags.remove(it) }
            }
        }
    }

    // tag segregated
    when (this) {
        PATH, NOT_ALLOWED, ALLOWED_ON_FOOTWAY, NON_DESIGNATED, EXCLUSIVE, EXCLUSIVE_WITH_SIDEWALK -> {
            tags.remove("segregated")
        }
        NON_SEGREGATED -> {
            tags["segregated"] = "no"
        }
        SEGREGATED -> {
            tags["segregated"] = "yes"
        }
    }

    // update check date
    if (!tags.hasChanges || tags.hasCheckDateForKey("bicycle")) {
        tags.updateCheckDateForKey("bicycle")
    }
}

private val noCycling = setOf(
    "no", "dismount"
)

private val yesButNotDesignated = setOf(
    "yes", "permissive", "private", "destination", "customers", "permit"
)
