package de.westnordost.streetcomplete.quests.surface

sealed class DetailSurfaceAnswer
data class DetailingImpossibleAnswer(val note: String) : DetailSurfaceAnswer()
data class SurfaceAnswer(val value: String) : DetailSurfaceAnswer()
