package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType.*

fun BuildingType.applyTo(tags: Tags) {
    if (osmKey == null || osmValue == null) return

    if (osmKey == "man_made") {
        tags.remove("building")
        tags["man_made"] = osmValue
    } else if (osmKey != "building") {
        tags[osmKey] = osmValue
        if (this == ABANDONED || this == RUINS) tags.remove("disused")
        if (this == RUINS) tags.remove("abandoned")
    } else {
        tags["building"] = osmValue
    }
}

// TODO do not change if stays the same
// TODO handle when non-building value is added/removed (e.g. historic=yes)
// TODO + tests
