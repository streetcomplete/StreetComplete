package de.westnordost.streetcomplete.quests.lanes

sealed interface LanesAnswer

data class MarkedLanes(val count: Int) : LanesAnswer
data object UnmarkedLanes : LanesAnswer
data class MarkedLanesSides(val forward: Int, val backward: Int, val centerLeftTurnLane: Boolean) : LanesAnswer

val LanesAnswer.total: Int? get() = when (this) {
    is MarkedLanes -> count
    is UnmarkedLanes -> null
    is MarkedLanesSides -> forward + backward + (if (centerLeftTurnLane) 1 else 0)
}
