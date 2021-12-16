package de.westnordost.streetcomplete.quests.surface

sealed class SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer()

sealed class SurfaceAnswer : SurfaceOrIsStepsAnswer()
data class GenericSurfaceAnswer(val value: Surface, val note: String) : SurfaceAnswer()
data class SpecificSurfaceAnswer(val value: Surface) : SurfaceAnswer()
