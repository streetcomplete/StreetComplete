package de.westnordost.streetcomplete.quests.address

import de.westnordost.streetcomplete.quests.localized_name.LocalizedName

sealed class AddressStreetAnswer(open val localizedNames: List<LocalizedName>)

data class StreetName(override val localizedNames: List<LocalizedName>) : AddressStreetAnswer(localizedNames)
data class PlaceName(override val localizedNames: List<LocalizedName>) : AddressStreetAnswer(localizedNames)
