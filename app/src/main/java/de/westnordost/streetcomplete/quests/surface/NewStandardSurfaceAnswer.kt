package de.westnordost.streetcomplete.quests.surface

sealed class DetailSurfaceAnswer
data class DetailingWhyOnlyGeneric(val value : String, val note: String) : DetailSurfaceAnswer()
data class SurfaceAnswer(val value: String) : DetailSurfaceAnswer()
