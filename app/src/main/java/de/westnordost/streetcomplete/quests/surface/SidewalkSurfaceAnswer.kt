package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.sidewalk_surface.LeftAndRightParsedSidewalkSurface
import de.westnordost.streetcomplete.osm.sidewalk_surface.applyTo
import de.westnordost.streetcomplete.osm.surface.ParsedSurfaceAndNote

sealed interface SidewalkSurfaceAnswer

data class SidewalkSurface(val surfaces: LeftAndRightSidewalkSurfaceAnswer) : SidewalkSurfaceAnswer
object SidewalkIsDifferent : SidewalkSurfaceAnswer

data class LeftAndRightSidewalkSurfaceAnswer(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
)

fun LeftAndRightSidewalkSurfaceAnswer.applyTo(tags: Tags) {
    val passedLeft = if (left != null) {
        ParsedSurfaceAndNote(left.surface, left.note)
    } else {
        null
    }
    val passedRight = if (right != null) {
        ParsedSurfaceAndNote(right.surface, right.note)
    } else {
        null
    }
    LeftAndRightParsedSidewalkSurface(passedLeft, passedRight).applyTo(tags)
}
