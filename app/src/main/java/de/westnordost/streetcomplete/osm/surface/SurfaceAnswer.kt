package de.westnordost.streetcomplete.osm.surface

import de.westnordost.streetcomplete.osm.Tags

data class SurfaceAnswer(val value: Surface, val note: String? = null)

fun SurfaceAnswer.applyTo(tags: Tags, prefix: String? = null, updateCheckDate: Boolean = true) {
    value.applyTo(tags, prefix, updateCheckDate, note)
}
