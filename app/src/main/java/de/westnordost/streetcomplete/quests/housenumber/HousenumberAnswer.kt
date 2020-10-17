package de.westnordost.streetcomplete.quests.housenumber

sealed class HousenumberAnswer

data class ConscriptionNumber(val number: String, val streetNumber: String? = null) : HousenumberAnswer()
data class HouseNumber(val number: String) : HousenumberAnswer()
data class HouseName(val name: String) : HousenumberAnswer()
data class HouseAndBlockNumber(val houseNumber: String, val blockNumber: String) : HousenumberAnswer()
object NoHouseNumber : HousenumberAnswer()
