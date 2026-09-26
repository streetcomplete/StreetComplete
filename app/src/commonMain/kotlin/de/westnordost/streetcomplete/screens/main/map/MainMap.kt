package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.interaction.ClickEvent
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.overlay.MapOverlayScope

/** Presents the shared map. */
@Composable
fun MainMap(
    state: MapState,
    modifier: Modifier = Modifier,
    onPan: () -> Unit = {},
    onRotate: () -> Unit = {},
    onMapClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    onMapLongClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    overlay: @Composable MapOverlayScope.() -> Unit = {},
) {
    MaplibreMap(
        modifier = modifier,
        state = state,
        cameraConstraints = CameraConstraints(minZoom = 0.0, maxZoom = 22.0),
        interactions = MapInteractions {
            camera {
                pan { onStart(onPan) }
                rotate { onStart(onRotate) }
            }
            callbacks {
                click { onUnhandled(onMapClick) }
                longClick { onEvent(onMapLongClick) }
            }
        },
        overlay = overlay,
    )
}
