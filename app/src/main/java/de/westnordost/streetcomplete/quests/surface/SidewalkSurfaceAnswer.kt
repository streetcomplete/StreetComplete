package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Surface

data class SidewalkSurfaceAnswer(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
)

data class SidewalkSurfaceSide(val surface: Surface)
