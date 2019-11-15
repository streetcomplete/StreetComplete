package de.westnordost.streetcomplete.quests.address

sealed class AddressStreetAnswer

data class StreetName(val name:String) : AddressStreetAnswer()
data class PlaceName(val name:String) : AddressStreetAnswer()
