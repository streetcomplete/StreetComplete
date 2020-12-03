package de.westnordost.streetcomplete.quests.place_name

sealed class PlaceNameAnswer

data class BrandFeature(val tags: Map<String, String>) : PlaceNameAnswer()
data class PlaceName(val name: String) : PlaceNameAnswer()
object NoPlaceNameSign : PlaceNameAnswer()
