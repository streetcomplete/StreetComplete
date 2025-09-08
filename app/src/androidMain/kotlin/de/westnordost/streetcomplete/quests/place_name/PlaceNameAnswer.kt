package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.osm.localized_name.LocalizedName

sealed interface PlaceNameAnswer {
    data object NoNameSign : PlaceNameAnswer

}

data class PlaceName(val localizedNames: List<LocalizedName>) : PlaceNameAnswer
