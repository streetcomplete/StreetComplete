package de.westnordost.streetcomplete.quests.lanes

import kotlinx.serialization.Serializable

@Serializable
sealed interface LanesAnswer

@Serializable
data class MarkedLanes(
    val forward: Int? = null,
    val backward: Int? = null,
    val centerLeftTurnLane: Boolean = false
) : LanesAnswer {
    val total: Int get() = (forward ?: 0) + (backward ?: 0) + (if (centerLeftTurnLane) 1 else 0)
}

@Serializable
data object UnmarkedLanes : LanesAnswer
