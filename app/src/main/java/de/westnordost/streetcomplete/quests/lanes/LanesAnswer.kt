package de.westnordost.streetcomplete.quests.lanes

sealed class LanesAnswer

data class MarkedLanes(val count: Int) : LanesAnswer()
data class UnmarkedLanes(val count: Int) : LanesAnswer()
data class MarkedLanesSides(val forward: Int, val backward: Int, val centerLeftTurnLane: Boolean) : LanesAnswer()

val LanesAnswer.total: Int get() = when(this) {
    is MarkedLanes -> count
    is UnmarkedLanes -> count
    is MarkedLanesSides -> forward + backward + (if (centerLeftTurnLane) 1 else 0)
}
