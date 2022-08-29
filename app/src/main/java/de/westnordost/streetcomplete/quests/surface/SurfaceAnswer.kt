package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isSurfaceAndTracktypeMismatching
import de.westnordost.streetcomplete.osm.removeCheckDatesForKey
import de.westnordost.streetcomplete.osm.updateWithCheckDate

sealed interface SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer
object IsIndoorsAnswer : SurfaceOrIsStepsAnswer

data class SurfaceAnswer(val value: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer

fun SurfaceAnswer.applyTo(tags: Tags, key: String) {
    val osmValue = value.osmValue
    val previousOsmValue = tags[key]

    val replacesTracktype = tags.containsKey("tracktype")
        && isSurfaceAndTracktypeMismatching(osmValue, tags["tracktype"]!!)

    if (replacesTracktype) {
        tags.remove("tracktype")
        tags.removeCheckDatesForKey("tracktype")
    }

    // update surface + check date
    tags.updateWithCheckDate(key, osmValue)
    // remove smoothness tag if surface was changed
    // or surface can be treated as outdated
    if ((previousOsmValue != null && previousOsmValue != osmValue) || replacesTracktype) {
        tags.remove("smoothness")
        tags.remove("smoothness:date")
        tags.removeCheckDatesForKey("smoothness")
    }
    // add/remove note - used to describe generic surfaces
    if (note != null) {
        tags["$key:note"] = note
    } else {
        tags.remove("$key:note")
    }
    // clean up old source tags - source should be in changeset tags
    tags.remove("source:$key")
}
