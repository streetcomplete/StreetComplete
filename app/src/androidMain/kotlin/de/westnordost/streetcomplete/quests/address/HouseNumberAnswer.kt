package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.osm.address.AddressNumber
import kotlinx.serialization.Serializable

sealed interface HouseNumberAnswer {
    data object WrongBuildingType : HouseNumberAnswer
}

@Serializable
data class AddressNumberAndName(val number: AddressNumber?, val name: String?) : HouseNumberAnswer {

    fun isEmpty(): Boolean =
        number?.isEmpty() != false && name?.isEmpty() != false

    fun isComplete(): Boolean =
        number?.isComplete() == true || name?.isNotEmpty() == true
}

