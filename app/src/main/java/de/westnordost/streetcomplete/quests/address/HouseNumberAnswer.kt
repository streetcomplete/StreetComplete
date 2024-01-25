package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.osm.address.AddressNumber

sealed interface HouseNumberAnswer

data class AddressNumberOrName(val number: AddressNumber?, val name: String?) : HouseNumberAnswer
data object WrongBuildingType : HouseNumberAnswer
