package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.surface.parseSurface

/** Returns either a [SingleSurface] or a [SegregatedSurface] if the footway is segregated between
 *  pedestrians and bicyclists */
fun parseSurfaceOverlayAnswer(tags: Map<String, String>): SurfaceOverlayAnswer {
    val originalSurface = parseSurface(tags["surface"])
    val originalCyclewaySurface = parseSurface(tags["cycleway:surface"])
    val originalFootwaySurface = parseSurface(tags["footway:surface"])
    val isSegregated =
        tags["highway"] in ALL_PATHS &&
        (tags["segregated"] == "yes" || originalCyclewaySurface != null || originalFootwaySurface != null)

    return if (isSegregated) {
        SegregatedSurface(footway = originalFootwaySurface, cycleway = originalCyclewaySurface)
    } else {
        SingleSurface(originalSurface)
    }
}
