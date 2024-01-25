package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.osm.LocalizedName

sealed interface BusStopNameAnswer

data object NoBusStopName : BusStopNameAnswer
data class BusStopName(val localizedNames: List<LocalizedName>) : BusStopNameAnswer
