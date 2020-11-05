package de.westnordost.streetcomplete.quests.lanes

sealed class LanesAnswer

data class MarkedLanes(val count: Int) : LanesAnswer() {
    init {
        require(count % 2 == 0 && count != 1) { "Lane count must be even" }
        require(count > 0) { "Lane count must greater than 0" }
    }
}
data class UnmarkedLanes(val count: Int) : LanesAnswer() {
    init {
        require(count > 0) { "Lane count must greater than 0" }
    }
}
data class MarkedLanesSides(val forward: Int, val backward: Int) : LanesAnswer() {
    init {
        require(forward > 0) { "Forward lane count must greater than 0" }
        require(backward > 0) { "Backward lane count must greater than 0" }
    }
}
