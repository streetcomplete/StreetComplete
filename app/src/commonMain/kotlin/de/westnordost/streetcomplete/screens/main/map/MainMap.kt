package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.map.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlayLayers
import de.westnordost.streetcomplete.screens.main.map.layers.StyleableOverlaySideLayer
import de.westnordost.streetcomplete.screens.main.map.layers.TracksLayers
import org.koin.compose.viewmodel.koinViewModel
import org.maplibre.compose.camera.CameraState
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.StyleState
import org.maplibre.compose.style.rememberStyleState

/**
 * MapLibre Map with StreetComplete theme and all stuff displayed on top of it
 * */
@Composable
fun MainMap(
    modifier: Modifier = Modifier,
    viewModel: MainMapViewModel = koinViewModel(),
    cameraState: CameraState = rememberCameraState(),
    styleState: StyleState = rememberStyleState(),
) {
    val downloadedTiles by viewModel.downloadedTiles.collectAsState()

    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Json(BASE_STYLE),
        zoomRange = 0f..22f,
        cameraState = cameraState,
        styleState = styleState,
        options = MapOptions(ornamentOptions = OrnamentOptions.AllDisabled)
    ) {
        val languages = listOf(Locale.current.language)
        val colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light

        MapStyle(
            colors = colors,
            languages = languages,
            belowRoadsContent = {
                // left-and-right lines should be rendered behind the actual road
                //TODO StyleableOverlaySideLayer(styleableOverlaySource, isBridge = false)
            },
            belowRoadsOnBridgeContent = {
                // left-and-right lines should be rendered behind the actual bridge road
                //TODO StyleableOverlaySideLayer(styleableOverlaySource, isBridge = true)
            },
            belowLabelsContent = {
                // labels should be on top of other layers
                DownloadedAreaLayer(downloadedTiles)
                //TODO StyleableOverlayLayers(styleableOverlaySource, onClickOverlay)
                //TODO TracksLayers(trackpoints, isRecording, oldTrackpointsLists)
            },
            aboveLabelsContent = {
                // these are always on top of everything else (including labels)
                //TODO StyleableOverlayLabelLayer(styleableOverlaySource, colors.text, colors.textOutline, onClickOverlay)
                //TODO GeometryMarkersLayers(markers)
                //TODO FocusedGeometryLayers(geometry)
                //TODO CurrentLocationLayers(location, rotation)
                //TODO PinsLayers(pins, onClickPin, onClickCluster)
                //TODO SelectedPinsLayer(iconPainter, pinPositions)
            }
        )
    }
}

// need to refer to the local (font) resources platform-independently
private val BASE_STYLE = """
    {
      "version": 8,
      "name": "Empty",
      "metadata": {},
      "sources": {},
      "glyphs": "${
        Res.getUri("files/glyphs/Roboto Regular/0-255.pbf")
            .replace("Roboto Regular", "{fontstack}")
            .replace("0-255", "{range}")
            // workaround for https://github.com/maplibre/maplibre-native/issues/4498
            .replace("file:///android_asset/", "asset://")
      }",
      "layers": []
    }
    """.trimIndent()
