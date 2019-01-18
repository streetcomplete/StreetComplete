package de.westnordost.streetcomplete.quests.bikeway

data class CyclewayAnswer(
    val left:CyclewaySide?,
    val right:CyclewaySide?,
    val isOnewayNotForCyclists:Boolean = false
)

data class CyclewaySide(val cycleway: Cycleway, val dirInOneway: Int = 0)
