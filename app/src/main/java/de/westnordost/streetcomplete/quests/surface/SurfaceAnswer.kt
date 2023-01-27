package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.ANYTHING_FULLY_PAVED
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isSurfaceAndTracktypeMismatching
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer
object IsIndoorsAnswer : SurfaceOrIsStepsAnswer

data class SurfaceAnswer(val value: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer

fun SurfaceAnswer.applyTo(tags: Tags, prefix: String? = null) {
    val osmValue = value.osmValue
    val pre = if (prefix != null) "$prefix:" else ""
    val key = "${pre}surface"
    val previousOsmValue = tags[key]

    var replacesTracktype = false
    if (prefix == null) {
        replacesTracktype = tags.containsKey("tracktype")
            && isSurfaceAndTracktypeMismatching(osmValue, tags["tracktype"]!!)

        if (replacesTracktype) {
            tags.remove("tracktype")
            tags.removeCheckDatesForKey("tracktype")
        }
    }

    // remove smoothness tag if surface was changed
    // or surface can be treated as outdated
    if ((previousOsmValue != null && previousOsmValue != osmValue) || replacesTracktype) {
        tags.remove("$key:grade")
        tags.remove("${pre}smoothness")
        tags.remove("${pre}smoothness:date")
        tags.remove("source:smoothness")
        tags.removeCheckDatesForKey("${pre}smoothness")
    }

    // update surface + check date
    tags.updateWithCheckDate(key, osmValue)

    // add/remove note - used to describe generic surfaces
    if (note != null) {
        tags["$key:note"] = note
    } else {
        tags.remove("$key:note")
    }
    // clean up old source tags - source should be in changeset tags
    tags.remove("source:$key")
}

fun SurfaceAnswer.updateSegregatedFootAndCycleway(tags: Tags) {
    val footwaySurface = tags["footway:surface"]
    val cyclewaySurface = tags["cycleway:surface"]
    if (cyclewaySurface != null && footwaySurface != null) {
        val commonSurface = when {
            footwaySurface == cyclewaySurface -> this
            footwaySurface in ANYTHING_FULLY_PAVED && cyclewaySurface in ANYTHING_FULLY_PAVED -> SurfaceAnswer(Surface.PAVED_ROAD)
            else -> null
        }
        if (commonSurface != null) {
            commonSurface.applyTo(tags)
        } else {
            removeSurface(tags)
        }
    }
}

private fun removeSurface(tags: Tags) {
    tags.remove("surface")
    tags.remove("surface:note")
    tags.remove("source:surface")
    tags.removeCheckDatesForKey("surface")
    tags.remove("surface:grade")
    tags.remove("smoothness")
    tags.remove("smoothness:date")
    tags.remove("source:smoothness")
    tags.removeCheckDatesForKey("smoothness")
}
