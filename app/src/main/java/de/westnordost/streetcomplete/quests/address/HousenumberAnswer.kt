package de.westnordost.streetcomplete.quests.address

sealed interface HousenumberAnswer

data class HouseNumberAndHouseName(val number: AddressNumber?, val name: String?) : HousenumberAnswer
object WrongBuildingType : HousenumberAnswer

sealed interface AddressNumber

data class ConscriptionNumber(val conscriptionNumber: String, val streetNumber: String? = null) : AddressNumber
data class HouseNumber(val houseNumber: String) : AddressNumber
data class HouseAndBlockNumber(val houseNumber: String, val blockNumber: String) : AddressNumber
