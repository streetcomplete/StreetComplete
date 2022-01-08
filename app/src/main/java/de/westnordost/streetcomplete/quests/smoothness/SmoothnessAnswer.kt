package de.westnordost.streetcomplete.quests.smoothness

sealed class SmoothnessAnswer

data class SmoothnessValueAnswer(val value: Smoothness): SmoothnessAnswer()

object WrongSurfaceAnswer: SmoothnessAnswer()
