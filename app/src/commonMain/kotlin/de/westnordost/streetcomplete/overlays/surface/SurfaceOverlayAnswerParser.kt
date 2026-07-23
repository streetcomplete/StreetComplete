package de.westnordost.streetcomplete.overlays.surface

import de.westnordost.streetcomplete.osm.ALL_PATHS
import de.westnordost.streetcomplete.osm.surface.parseSurface

/** Returns either a [SingleSurface] or a [SegregatedSurface] if the footway is segregated between
 *  pedestrians and bicyclists */
fun parseSurfaceOverlayAnswer(tags: Map<String, String>): SurfaceOverlayAnswer {
    val surface = parseSurface(tags["surface"])
    val cyclewaySurface = parseSurface(tags["cycleway:surface"])
    val footwaySurface = parseSurface(tags["footway:surface"])
    val isSegregated =
        tags["highway"] in ALL_PATHS &&
        (tags["segregated"] == "yes" || cyclewaySurface != null || footwaySurface != null)

    return if (isSegregated) {
        SegregatedSurface(footway = footwaySurface, cycleway = cyclewaySurface)
    } else {
        SingleSurface(surface)
    }
}
