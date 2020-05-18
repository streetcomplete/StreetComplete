package de.westnordost.streetcomplete.quests.address

sealed class AddressStreetAnswer(open val name: String)

data class StreetName(override val name: String) : AddressStreetAnswer(name)
data class PlaceName(override val name: String) : AddressStreetAnswer(name)
