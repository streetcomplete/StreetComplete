package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.quests.LocalizedName

sealed class PlaceNameAnswer

data class PlaceName(val localizedNames: List<LocalizedName>) : PlaceNameAnswer()
object NoPlaceNameSign : PlaceNameAnswer()
