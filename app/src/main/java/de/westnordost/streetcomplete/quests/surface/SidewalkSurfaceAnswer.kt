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
    fun applyTo(tags: StringMapChangesBuilder) {
        when (this) {
            is SidewalkIsDifferent -> {
                deleteSmoothnessKeys(Side.LEFT, tags)
                deleteSmoothnessKeys(Side.RIGHT, tags)
                deleteSmoothnessKeys(Side.BOTH, tags)
                deleteSidewalkSurfaceAnswerIfExists(Side.LEFT, tags)
                deleteSidewalkSurfaceAnswerIfExists(Side.RIGHT, tags)
                deleteSidewalkSurfaceAnswerIfExists(Side.BOTH, tags)
                tags.remove("sidewalk:left")
                tags.remove("sidewalk:right")
                tags.remove("sidewalk:both")
                tags.remove("sidewalk")
            }
            is SidewalkSurface -> {
                val leftChanged = this.left?.let { sideSurfaceChanged(it, Side.LEFT, tags) }
                val rightChanged = this.right?.let { sideSurfaceChanged(it, Side.RIGHT, tags) }

                if (leftChanged == true) {
                    deleteSmoothnessKeys(Side.LEFT, tags)
                    deleteSmoothnessKeys(Side.BOTH, tags)
                }
                if (rightChanged == true) {
                    deleteSmoothnessKeys(Side.RIGHT, tags)
                    deleteSmoothnessKeys(Side.BOTH, tags)
                }

                if (this.left == this.right) {
                    this.left?.let { applySidewalkSurfaceAnswerTo(it, Side.BOTH, tags) }
                    deleteSidewalkSurfaceAnswerIfExists(Side.LEFT, tags)
                    deleteSidewalkSurfaceAnswerIfExists(Side.RIGHT, tags)
                } else {
                    this.left?.let { applySidewalkSurfaceAnswerTo(it, Side.LEFT, tags) }
                    this.right?.let { applySidewalkSurfaceAnswerTo(it, Side.RIGHT, tags) }
                    deleteSidewalkSurfaceAnswerIfExists(Side.BOTH, tags)
                }
            }
        }

        deleteSidewalkSurfaceAnswerIfExists(null, tags)

        // only set the check date if nothing was changed or if check date was already set
        if (!tags.hasChanges || tags.hasCheckDateForKey("sidewalk:surface")) {
            tags.updateCheckDateForKey("sidewalk:surface")
        }
    }

    private fun sideSurfaceChanged(surface: SurfaceAnswer, side: Side, tags: Tags): Boolean {
        val previousSideOsmValue = tags["sidewalk:${side.value}:surface"]
        val previousBothOsmValue = tags["sidewalk:both:surface"]
        val osmValue = surface.value.osmValue

        return previousSideOsmValue != null && previousSideOsmValue != osmValue
            || previousBothOsmValue != null && previousBothOsmValue != osmValue
    }

    private enum class Side(val value: String) {
        LEFT("left"), RIGHT("right"), BOTH("both")
    }

    private fun applySidewalkSurfaceAnswerTo(surface: SurfaceAnswer, side: Side, tags: Tags) {
        val sidewalkKey = "sidewalk:" + side.value
        val sidewalkSurfaceKey = "$sidewalkKey:surface"

        tags[sidewalkSurfaceKey] = surface.value.osmValue

        // add/remove note - used to describe generic surfaces
        if (surface.note != null) {
            tags["$sidewalkSurfaceKey:note"] = surface.note
        } else {
            tags.remove("$sidewalkSurfaceKey:note")
        }
        // clean up old source tags - source should be in changeset tags
        tags.remove("source:$sidewalkSurfaceKey")
    }

    /** clear smoothness tags for the given side*/
    private fun deleteSmoothnessKeys(side: Side, tags: Tags) {
        val sidewalkKey = "sidewalk:" + side.value
        tags.remove("$sidewalkKey:smoothness")
        tags.remove("$sidewalkKey:smoothness:date")
        tags.removeCheckDatesForKey("$sidewalkKey:smoothness")
    }

    /** clear previous answers for the given side */
    private fun deleteSidewalkSurfaceAnswerIfExists(side: Side?, tags: Tags) {
        val sideVal = if (side == null) "" else ":" + side.value
        val sidewalkSurfaceKey = "sidewalk$sideVal:surface"

        // only things are cleared that are set by this quest
        // for example cycleway:surface should only be cleared by a cycleway surface quest etc.
        tags.remove(sidewalkSurfaceKey)
        tags.remove("$sidewalkSurfaceKey:note")
    }
}
