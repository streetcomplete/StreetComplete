package de.westnordost.streetcomplete.quests.smoothness

sealed class SmoothnessAnswer

data class SmoothnessValueAnswer(val osmValue: String): SmoothnessAnswer()

object WrongSurfaceAnswer: SmoothnessAnswer()
