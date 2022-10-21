package de.westnordost.streetcomplete.quests.surface

sealed interface SidewalkSurfaceAnswer

data class SidewalkSurface(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
) : SidewalkSurfaceAnswer

object SidewalkIsDifferent : SidewalkSurfaceAnswer

data class SidewalkSurfaceSide(val surface: Surface)

