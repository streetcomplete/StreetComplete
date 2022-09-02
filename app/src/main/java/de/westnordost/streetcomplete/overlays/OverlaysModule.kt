package de.westnordost.streetcomplete.overlays

import de.westnordost.streetcomplete.data.overlays.OverlayRegistry
import de.westnordost.streetcomplete.overlays.sidewalk.SidewalkOverlay
import de.westnordost.streetcomplete.overlays.surface.UniversalSurfaceOverlay
import de.westnordost.streetcomplete.overlays.surface.RoadSurfaceOverlay
import de.westnordost.streetcomplete.overlays.surface.PathSurfaceOverlay
//import de.westnordost.streetcomplete.overlays.surface.SidewalkSurfaceOverlay
import de.westnordost.streetcomplete.overlays.tracktype.TracktypeOverlay
import de.westnordost.streetcomplete.overlays.way_lit.WayLitOverlay
import org.koin.dsl.module

val overlaysModule = module {
    single { OverlayRegistry(listOf(
        WayLitOverlay(),
        SidewalkOverlay(),
        TracktypeOverlay(),
        RoadSurfaceOverlay(),
        PathSurfaceOverlay(),
        UniversalSurfaceOverlay(),
        //SidewalkSurfaceOverlay(),
    )) }
}
