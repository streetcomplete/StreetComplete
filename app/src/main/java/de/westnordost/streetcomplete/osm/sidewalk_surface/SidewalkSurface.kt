package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.ParsedSurfaceAndNote
import de.westnordost.streetcomplete.quests.surface.SurfaceAnswer

data class LeftAndRightSidewalkSurfaceAnswer(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
)

data class LeftAndRightParsedSidewalkSurface(
    val left: ParsedSurfaceAndNote?,
    val right: ParsedSurfaceAndNote?,
)
