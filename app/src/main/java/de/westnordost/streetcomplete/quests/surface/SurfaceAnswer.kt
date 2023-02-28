package de.westnordost.streetcomplete.quests.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo

sealed interface SurfaceAnswer
object IsActuallyStepsAnswer : SurfaceAnswer
object IsIndoorsAnswer : SurfaceAnswer
data class IsSurfaceAnswer(val surface: Surface, val note: String? = null) : SurfaceAnswer {
    fun applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true, note: String? = null) {
        surface.applyTo(tags, prefix = prefix, updateCheckDate = updateCheckDate, note = note)
    }
}
