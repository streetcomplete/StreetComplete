package de.westnordost.streetcomplete.quests.bus_stop_name

import de.westnordost.streetcomplete.quests.LocalizedName

sealed class BusStopNameAnswer

object NoBusStopName : BusStopNameAnswer()
data class BusStopName(val localizedNames: List<LocalizedName>) : BusStopNameAnswer()
