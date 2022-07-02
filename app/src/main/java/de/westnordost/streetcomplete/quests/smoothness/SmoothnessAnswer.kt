package de.westnordost.streetcomplete.quests.smoothness

sealed interface SmoothnessAnswer

data class SmoothnessValueAnswer(val value: Smoothness) : SmoothnessAnswer

object IsActuallyStepsAnswer : SmoothnessAnswer
object WrongSurfaceAnswer : SmoothnessAnswer
