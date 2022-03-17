package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.quests.LocalizedName

sealed interface PlaceNameAnswer

data class PlaceName(val localizedNames: List<LocalizedName>) : PlaceNameAnswer
object NoPlaceNameSign : PlaceNameAnswer
