package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.maplibre.compose.interaction.ClickEvent
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.interaction.MapInteractions
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap

/** Presents the shared map. */
@Composable
fun MainMap(
    state: MapState,
    modifier: Modifier = Modifier,
    onMapClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
    onMapLongClick: (ClickEvent) -> ClickResult = { ClickResult.Pass },
) {
    MaplibreMap(
        modifier = modifier,
        state = state,
        cameraConstraints = CameraConstraints(minZoom = 0.0, maxZoom = 22.0),
        interactions = MapInteractions {
            callbacks {
                click { onUnhandled(onMapClick) }
                longClick { onEvent(onMapLongClick) }
            }
        },
        overlay = {},
    )
}
