package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.address.AddressNumber
import de.westnordost.streetcomplete.osm.address.applyTo
import kotlinx.serialization.Serializable

sealed interface HouseNumberAnswer {
    data object WrongBuildingType : HouseNumberAnswer
}

@Serializable
data class AddressNumberAndName(val number: AddressNumber?, val name: String?) : HouseNumberAnswer {

    fun isEmpty(): Boolean =
        number?.isEmpty() != false && name?.isEmpty() != false

    fun isComplete(): Boolean =
        number?.isComplete() == true || name?.isNotEmpty() == true && number?.isEmpty() != false
}

fun AddressNumberAndName.applyTo(tags: Tags, countryCode: String?) {
    number?.applyTo(tags, countryCode)
    if (!name.isNullOrEmpty()) {
        tags["addr:housename"] = name
    }
}
