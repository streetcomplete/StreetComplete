package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.osm.LocalizedName

sealed interface PlaceNameAnswer

data class PlaceName(val localizedNames: List<LocalizedName>) : PlaceNameAnswer
data object NoPlaceNameSign : PlaceNameAnswer
