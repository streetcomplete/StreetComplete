package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightSidewalkSurfaceAnswer

sealed interface SidewalkSurfaceAnswer

data class SidewalkSurface(val surfaces: LeftAndRightSidewalkSurfaceAnswer) : SidewalkSurfaceAnswer
object SidewalkIsDifferent : SidewalkSurfaceAnswer
