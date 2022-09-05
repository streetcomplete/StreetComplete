package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.osm.housenumber.AddressNumber

sealed interface HouseNumberAnswer

data class HouseNumberAndHouseName(val number: AddressNumber?, val name: String?) : HouseNumberAnswer
object WrongBuildingType : HouseNumberAnswer
