package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType.*
import de.westnordost.streetcomplete.osm.updateCheckDate

fun BuildingType.applyTo(tags: Tags) {
    require(osmKey != null && osmValue != null)

    // do not change anything if it is considered an alias! (This could destroy information, e.g.
    // building=livestock would be changed to building=farm_auxiliary)
    val alias = BuildingType.aliases.entries.find { tags[it.key.first] == it.key.second }?.value
    if (alias == this) {
        tags.updateCheckDate()
        return
    }

    // clear the *=yes tags, after that, re-add if this was selected
    listOf("disused", "abandoned", "ruins", "historic").forEach {
        tags.remove(it)
    }

    // switch between man-made and building
    if (osmKey == "man_made") tags.remove("building")
    if (osmKey == "building") tags.remove("man_made")

    tags[osmKey] = osmValue

    // we set the check date and not check_date:building because this is about the primary feature,
    // not a property of a feature.
    if (!tags.hasChanges) {
        tags.updateCheckDate()
    }
}
