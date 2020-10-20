package de.westnordost.streetcomplete.quests.surface

sealed class SurfaceAnswer
data class GenericSurfaceAnswer(val value : String, val note: String) : SurfaceAnswer()
data class SpecificSurfaceAnswer(val value: String) : SurfaceAnswer()
