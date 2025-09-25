package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.surface.Surface
import de.westnordost.streetcomplete.osm.surface.applyTo
import de.westnordost.streetcomplete.osm.surface.parseSurface
import de.westnordost.streetcomplete.osm.surface.updateCommonSurfaceFromFootAndCyclewaySurface
import kotlin.jvm.JvmInline

sealed interface SurfaceOverlayAnswer {
    fun isComplete(): Boolean
    /** only non-null if complete*/
    fun serializeToString(): String?

    companion object {
        fun deserializeFromString(value: String): SurfaceOverlayAnswer? =
            SegregatedSurface.deserializeFromString(value)
                ?: SingleSurface.deserializeFromString(value)
    }
}
@JvmInline value class SingleSurface(val value: Surface?) : SurfaceOverlayAnswer {
    override fun isComplete(): Boolean =
        value != null

    override fun serializeToString(): String? =
        value?.name

    companion object {
        fun deserializeFromString(value: String): SingleSurface? =
            SingleSurface(Surface.valueOf(value))
    }
}
data class SegregatedSurface(val footway: Surface?, val cycleway: Surface?) : SurfaceOverlayAnswer {
    override fun isComplete(): Boolean =
        footway != null && cycleway != null

    override fun serializeToString(): String? =
        if (footway != null && cycleway != null) footway.name + "+" + cycleway.name else null

    companion object {
        fun deserializeFromString(value: String): SegregatedSurface? {
            val splits = value.split('+', limit = 2)
            if (splits.size != 2) return null
            return SegregatedSurface(
                footway = Surface.valueOf(splits[0]),
                cycleway = Surface.valueOf(splits[1])
            )
        }
    }
}

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
