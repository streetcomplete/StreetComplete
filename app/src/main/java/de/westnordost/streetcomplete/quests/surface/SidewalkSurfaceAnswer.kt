package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer

sealed interface SidewalkSurfaceAnswer {
    enum class Side(val value: String) {
        LEFT("left"),
        RIGHT("right"),
        BOTH("both")
    }
}

data class SidewalkSurface(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
) : SidewalkSurfaceAnswer

object SidewalkIsDifferent : SidewalkSurfaceAnswer
