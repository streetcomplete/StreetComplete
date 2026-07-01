package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import kotlinx.serialization.Serializable

@Serializable
sealed interface SurfaceOverlayAnswer

@Serializable
data class SingleSurface(val value: Surface?) : SurfaceOverlayAnswer

@Serializable
data class SegregatedSurface(val footway: Surface?, val cycleway: Surface?) : SurfaceOverlayAnswer

fun SurfaceOverlayAnswer.applyTo(tags: Tags) {
    when (this) {
        is SegregatedSurface -> {
            tags["segregated"] = "yes"
            footway?.applyTo(tags, "footway")
            cycleway?.applyTo(tags, "cycleway")
            updateCommonSurfaceFromFootAndCyclewaySurface(tags)
        }
        is SingleSurface -> {
            value?.applyTo(tags)
        }
    }
}
