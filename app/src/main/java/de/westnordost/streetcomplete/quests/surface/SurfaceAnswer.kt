package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.meta.removeCheckDatesForKey
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.Tags

sealed class SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer()

data class SurfaceAnswer(val value: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer()

fun SurfaceAnswer.applyTo(tags: Tags, key: String) {
    val osmValue = value.osmValue
    val previousOsmValue = tags[key]

    // update surface + check date
    tags.updateWithCheckDate(key, osmValue)
    // remove smoothness tag if surface was changed
    if (previousOsmValue != null && previousOsmValue != osmValue) {
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
