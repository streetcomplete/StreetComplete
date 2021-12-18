package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

sealed class SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer()

data class SurfaceAnswer(val value: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer()

fun SurfaceAnswer.applyTo(changes: StringMapChangesBuilder, key: String) {
    changes.updateWithCheckDate(key, value.osmValue)
    // add/remove note - used to describe generic surfaces
    if (note != null) {
        changes.addOrModify("$key:note", note)
    } else {
        changes.deleteIfExists("$key:note")
    }
    // clean up old source tags - source should be in changeset tags
    changes.deleteIfExists("source:$key")
}
