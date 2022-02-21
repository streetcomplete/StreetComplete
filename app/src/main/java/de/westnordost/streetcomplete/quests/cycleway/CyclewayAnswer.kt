package de.westnordost.streetcomplete.quests.cycleway

import de.westnordost.streetcomplete.osm.cycleway.Cycleway

data class CyclewayAnswer(
    val left: CyclewaySide?,
    val right: CyclewaySide?,
    val isOnewayNotForCyclists: Boolean = false
)

data class CyclewaySide(val cycleway: Cycleway, val dirInOneway: Int = 0)
