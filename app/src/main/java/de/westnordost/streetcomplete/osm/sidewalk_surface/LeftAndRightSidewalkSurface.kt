package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.SurfaceAndNote

data class LeftAndRightSidewalkSurface(
    val left: SurfaceAndNote?,
    val right: SurfaceAndNote?,
)
