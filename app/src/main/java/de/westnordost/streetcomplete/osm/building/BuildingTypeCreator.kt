package de.westnordost.streetcomplete.osm.building

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.building.BuildingType.*

fun BuildingType.applyTo(tags: Tags) {
    if (osmKey == "man_made") {
        tags.remove("building")
        tags["man_made"] = osmValue
    } else if (osmKey != "building") {
        tags[osmKey] = osmValue
        if (this == ABANDONED) {
            tags.remove("disused")
        }
        if (this == RUINS && tags["disused"] == "no") {
            tags.remove("disused")
        }
        if (this == RUINS && tags["abandoned"] == "no") {
            tags.remove("abandoned")
        }
    } else {
        tags["building"] = osmValue
    }
}
