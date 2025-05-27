package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightSidewalkSurface

sealed interface SidewalkSurfaceAnswer

data class SidewalkSurface(val value: LeftAndRightSidewalkSurface) : SidewalkSurfaceAnswer
data object SidewalkIsDifferent : SidewalkSurfaceAnswer
