package de.westnordost.streetcomplete.osm.address

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.quests.address.AddressNumberAndName
import kotlinx.serialization.Serializable

@Serializable
data class Address(
    val streetOrPlace: StreetOrPlaceName,
    val number: AddressNumber?,
    val name: String?,
)

fun Address.applyTo(tags: Tags, countryCode: String) {
    number?.applyTo(tags, countryCode)
    if (!name.isNullOrBlank()) {
        tags["addr:housename"] = name.trim()
    }
    number?.applyTo(tags, countryCode)
    streetOrPlace.applyTo(tags)
}
