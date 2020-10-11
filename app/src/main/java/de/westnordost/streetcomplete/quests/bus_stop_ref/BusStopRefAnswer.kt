package de.westnordost.streetcomplete.quests.bus_stop_ref

sealed class BusStopRefAnswer

object NoBusStopRef : BusStopRefAnswer()
data class BusStopRef(val ref: String) : BusStopRefAnswer()
