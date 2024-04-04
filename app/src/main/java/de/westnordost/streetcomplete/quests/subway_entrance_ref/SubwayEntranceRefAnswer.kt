package de.westnordost.streetcomplete.quests.subway_entrance_ref

sealed interface SubwayEntranceRefAnswer

data class SubwayEntranceRef(val ref: String) : SubwayEntranceRefAnswer
object NoVisibleSubwayEntranceRef : SubwayEntranceRefAnswer
