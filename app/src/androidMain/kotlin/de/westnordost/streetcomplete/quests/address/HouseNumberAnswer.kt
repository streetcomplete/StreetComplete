package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.osm.address.AddressNumber
import kotlinx.serialization.Serializable

sealed interface HouseNumberAnswer {
    data object WrongBuildingType : HouseNumberAnswer
}

@Serializable
data class AddressNumberOrName(val number: AddressNumber?, val name: String?) : HouseNumberAnswer

