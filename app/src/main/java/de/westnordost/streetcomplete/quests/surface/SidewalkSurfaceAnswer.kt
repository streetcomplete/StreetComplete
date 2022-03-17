package de.westnordost.streetcomplete.quests.surface

data class SidewalkSurfaceAnswer(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
)

data class SidewalkSurfaceSide(val surface: Surface)
