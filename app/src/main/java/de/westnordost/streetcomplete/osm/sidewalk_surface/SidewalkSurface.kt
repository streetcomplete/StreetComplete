package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.SurfaceWithNote
import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

data class LeftAndRightSidewalkSurface(
    val left: SurfaceAndNote?,
    val right: SurfaceAndNote?,
)

data class LeftAndRightParsedSidewalkSurface(
    val left: SurfaceWithNote?,
    val right: SurfaceWithNote?,
)
