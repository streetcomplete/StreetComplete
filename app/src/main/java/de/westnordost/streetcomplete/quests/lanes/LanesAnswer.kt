package de.westnordost.streetcomplete.quests.lanes

sealed class LanesAnswer

data class MarkedLanes(val count: Int) : LanesAnswer()
data class UnmarkedLanes(val count: Int) : LanesAnswer()
data class MarkedLanesSides(val forward: Int, val backward: Int) : LanesAnswer()
