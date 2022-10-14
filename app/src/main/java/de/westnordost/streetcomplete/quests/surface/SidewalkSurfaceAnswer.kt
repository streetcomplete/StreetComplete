package de.westnordost.streetcomplete.quests.surface

sealed interface SidewalkSurfaceAnswer
object SidewalkIsDifferent : SidewalkSurfaceAnswer

data class SidewalkSurface(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
) : SidewalkSurfaceAnswer

data class SidewalkSurfaceSide(val surface: Surface)
