package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.sidewalk_surface.SidewalkSurface

sealed interface SidewalkSurfaceAnswer {
    data object SidewalkIsDifferent : SidewalkSurfaceAnswer
    @JvmInline value class Surfaces(val value: SidewalkSurface) : SidewalkSurfaceAnswer
}
