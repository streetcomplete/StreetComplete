package de.westnordost.streetcomplete.quests.bus_stop_ref

sealed interface BusStopRefAnswer

object NoVisibleBusStopRef : BusStopRefAnswer
data class BusStopRef(val ref: String) : BusStopRefAnswer
