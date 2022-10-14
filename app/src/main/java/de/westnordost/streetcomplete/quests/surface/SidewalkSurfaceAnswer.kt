package de.westnordost.streetcomplete.quests.surface

sealed interface SidewalkSurfaceAnswer
object SidewalkIsDifferent : SidewalkSurfaceAnswer

data class SidewalkSurfaceAnswer(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
)

data class SidewalkSurfaceSide(val surface: Surface)
