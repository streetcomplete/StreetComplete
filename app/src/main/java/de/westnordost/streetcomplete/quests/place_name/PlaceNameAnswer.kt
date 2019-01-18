package de.westnordost.streetcomplete.quests.place_name

sealed class PlaceNameAnswer

data class PlaceName(val name:String) : PlaceNameAnswer()
object NoPlaceNameSign : PlaceNameAnswer()
