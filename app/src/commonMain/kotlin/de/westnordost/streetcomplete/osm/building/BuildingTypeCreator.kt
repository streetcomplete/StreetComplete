package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.updateCheckDate

fun BuildingType.applyTo(tags: Tags, buildingUse: BuildingType? = null) {
    require(osmKey != null && osmValue != null)

    // do not change anything if it is considered an alias! (This could destroy information, e.g.
    // building=livestock would be changed to building=farm_auxiliary)
    val alias = BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value

    if (alias != this) {
        // Clear existing lifecycle tags before applying the selected type.
        listOf("disused", "abandoned", "ruins").forEach { key -> tags.remove(key) }

        // Switch between man-made and building.
        if (osmKey == "man_made") tags.remove("building")
        if (osmKey == "building") tags.remove("man_made")

        if (this == TERRACED_HOUSE) tags["building"] = "house"

        // Apply the original building classification.
        tags[osmKey] = osmValue
    }

    // Apply the selected current use/lifecycle state.
    // This must happen after the original building classification is updated,
    // so the comparison uses the resulting building tag.
    buildingUse?.let {
        require(it.osmKey != null && it.osmValue != null)

        listOf("disused", "abandoned", "ruins").forEach { key -> tags.remove(key) }

        when {
            // Ruins, abandoned and building:use are mutually exclusive states
            it.osmKey == "ruins" -> {
                tags["ruins"] = "yes"
                tags.remove("building:use")
            }

            it.osmKey == "abandoned" -> {
                tags["abandoned"] = "yes"
                tags.remove("building:use")
            }

            // if user selects building:use same as building tag
            // then there is no need for a separate building:use tag.
            it.osmValue == tags["building"] -> tags.remove("building:use")

            else -> tags["building:use"] = it.osmValue
        }
    }

    // Update check_date only when neither selection changed the tags.
    if (!tags.hasChanges) {
        tags.updateCheckDate()
    }
}
