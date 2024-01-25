package de.westnordost.streetcomplete.osm.bicycle_boulevard

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.bicycle_boulevard.BicycleBoulevard.*

enum class BicycleBoulevard { YES, NO }

fun parseBicycleBoulevard(tags: Map<String, String>): BicycleBoulevard =
    // "no" value is extremely uncommon, hence the lack of this tag can be interpreted as "no"
    if (tags["bicycle_road"] == "yes" || tags["cyclestreet"] == "yes") YES else NO

fun BicycleBoulevard.applyTo(tags: Tags, countryCode: String) {
    when (this) {
        YES -> {
            // do not re-tag to different key if one already exists
            val useBicycleRoad = when {
                tags.containsKey("bicycle_road") -> true
                tags.containsKey("cyclestreet") -> false
                // in BeNeLux countries, cyclestreet established itself instead
                countryCode in listOf("BE", "NL", "LU") -> false
                else -> true
            }
            if (useBicycleRoad) {
                tags["bicycle_road"] = "yes"
                tags.remove("cyclestreet")
            } else {
                tags["cyclestreet"] = "yes"
                tags.remove("bicycle_road")
            }
        }
        NO -> {
            tags.remove("bicycle_road")
            tags.remove("cyclestreet")
        }
    }
}
