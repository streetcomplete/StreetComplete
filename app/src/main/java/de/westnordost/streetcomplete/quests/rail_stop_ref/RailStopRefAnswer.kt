package de.westnordost.streetcomplete.quests.rail_stop_ref

sealed interface RailStopRefAnswer

object NoRailStopRef : RailStopRefAnswer
data class RailStopRef(val ref: String) : RailStopRefAnswer
