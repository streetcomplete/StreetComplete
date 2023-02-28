package de.westnordost.streetcomplete.osm.sidewalk_surface

import de.westnordost.streetcomplete.osm.surface.ParsedSurfaceAndNote
import de.westnordost.streetcomplete.quests.surface.IsSurfaceAnswer

data class LeftAndRightSidewalkSurfaceAnswer(
    val left: IsSurfaceAnswer?,
    val right: IsSurfaceAnswer?,
)

data class LeftAndRightParsedSidewalkSurface(
    val left: ParsedSurfaceAndNote?,
    val right: ParsedSurfaceAndNote?,
)
