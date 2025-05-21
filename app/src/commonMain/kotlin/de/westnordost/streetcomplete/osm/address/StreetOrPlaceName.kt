package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags

sealed interface StreetOrPlaceName

data class StreetName(val name: String) : StreetOrPlaceName
data class PlaceName(val name: String) : StreetOrPlaceName

fun StreetOrPlaceName.applyTo(tags: Tags) {
    when (this) {
        is PlaceName -> tags["addr:place"] = name
        is StreetName -> tags["addr:street"] = name
    }
}
