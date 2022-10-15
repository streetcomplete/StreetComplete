package de.westnordost.streetcomplete.quests.surface

data class SidewalkSurfaceSide(val surface: Surface)

sealed interface SidewalkSurfaceAnswer
object SidewalkIsDifferent : SidewalkSurfaceAnswer

data class SidewalkSurface(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
) : SidewalkSurfaceAnswer

