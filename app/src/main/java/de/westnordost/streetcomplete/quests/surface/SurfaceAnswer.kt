package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

sealed class SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer()

data class SurfaceAnswer(val value: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer()

fun SurfaceAnswer.applyTo(changes: StringMapChangesBuilder, key: String) {
    val osmValue = value.osmValue
    val previousOsmValue = changes.getPreviousValue(key)

    // update surface + check date
    changes.updateWithCheckDate(key, osmValue)
    // remove smoothness tag if surface was changed
    if (previousOsmValue != null && previousOsmValue != osmValue) {
        changes.deleteIfExists("smoothness")
    }
    // add/remove note - used to describe generic surfaces
    if (note != null) {
        changes.addOrModify("$key:note", note)
    } else {
        changes.deleteIfExists("$key:note")
    }
    // clean up old source tags - source should be in changeset tags
    changes.deleteIfExists("source:$key")
}
