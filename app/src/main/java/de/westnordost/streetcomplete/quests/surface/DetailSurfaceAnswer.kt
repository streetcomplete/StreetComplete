package de.westnordost.streetcomplete.quests.surface

sealed class DetailSurfaceAnswer(open val value: String)

data class DetailingImpossibleAnswer(override val value: String) : DetailSurfaceAnswer(value)
data class SurfaceAnswer(override val value: String) : DetailSurfaceAnswer(value)
