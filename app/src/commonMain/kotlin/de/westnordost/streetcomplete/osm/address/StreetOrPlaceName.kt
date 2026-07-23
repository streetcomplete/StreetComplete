package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags
import kotlinx.serialization.Serializable

@Serializable
sealed interface StreetOrPlaceName {
    val name: String
}

@Serializable
data class StreetName(override val name: String) : StreetOrPlaceName
@Serializable
data class PlaceName(override val name: String) : StreetOrPlaceName

fun StreetOrPlaceName.applyTo(tags: Tags) {
    tags.remove("addr:street")

    if (name.isEmpty()) {
        if (this is PlaceName) {
            tags.remove("addr:place")
        }
    } else {
        when (this) {
            is PlaceName -> tags["addr:place"] = name
            is StreetName -> tags["addr:street"] = name
        }
    }
}
