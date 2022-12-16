package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.SurfaceAnswer
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

data class SidewalkSurface(
    val left: SurfaceAnswer?,
    val right: SurfaceAnswer?,
) : SidewalkSurfaceAnswer()

object SidewalkIsDifferent : SidewalkSurfaceAnswer()

data class SidewalkSurfaceSide(val surface: Surface)

open class SidewalkSurfaceAnswer {
    internal enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }
}
