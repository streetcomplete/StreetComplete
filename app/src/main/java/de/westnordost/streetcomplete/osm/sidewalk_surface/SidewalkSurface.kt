package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.ParsedSurfaceAndNote

data class LeftAndRightParsedSidewalkSurface(
    val left: ParsedSurfaceAndNote?,
    val right: ParsedSurfaceAndNote?,
)
