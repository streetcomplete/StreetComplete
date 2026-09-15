package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
    cameraPadding: PaddingValues = PaddingValues(0.dp),
    onPan: () -> Unit = {},
    onUserCameraMove: () -> Unit = {},
    onMapClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    onMapLongClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    overlay: @Composable MapOverlayScope.() -> Unit = {},
) {
    MaplibreMap(
        modifier = modifier,
        state = state,
        cameraPadding = cameraPadding,
        cameraConstraints = CameraConstraints(minZoom = 0.0, maxZoom = 22.0),
        interactions = MapInteractions {
            camera {
                pan {
                    onStart {
                        onPan()
                        onUserCameraMove()
                    }
                }
                zoom { onStart(onUserCameraMove) }
                rotate { onStart(onUserCameraMove) }
                tilt { onStart(onUserCameraMove) }
            }
            callbacks {
                click { onUnhandled(onMapClick) }
                longClick { onEvent(onMapLongClick) }
            }
        },
        overlay = overlay,
    )
}
