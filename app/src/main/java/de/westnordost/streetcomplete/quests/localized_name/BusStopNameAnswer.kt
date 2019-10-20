package de.westnordost.streetcomplete.quests.localized_name

sealed class BusStopNameAnswer

object NoBusStopName : BusStopNameAnswer()
data class BusStopName(val localizedNames:List<LocalizedName>) : BusStopNameAnswer()
