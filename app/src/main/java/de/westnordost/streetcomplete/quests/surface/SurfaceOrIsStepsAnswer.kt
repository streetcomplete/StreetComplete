package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo

sealed interface SurfaceOrIsStepsAnswer
object IsActuallyStepsAnswer : SurfaceOrIsStepsAnswer
object IsIndoorsAnswer : SurfaceOrIsStepsAnswer
data class SurfaceAnswer(val surface: Surface, val note: String? = null) : SurfaceOrIsStepsAnswer {
    fun applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true, note: String? = null) {
        surface.applyTo(tags, prefix = prefix, updateCheckDate = updateCheckDate, note = note)
    }
}
