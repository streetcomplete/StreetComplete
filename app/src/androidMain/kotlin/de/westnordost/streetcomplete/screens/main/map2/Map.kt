package de.westnordost.streetcomplete.screens.main.map2

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.screens.main.map2.layers.CurrentLocationLayers
import de.westnordost.streetcomplete.screens.main.map2.layers.DownloadedAreaLayer
import de.westnordost.streetcomplete.screens.main.map2.layers.FocusedGeometryLayers
import de.westnordost.streetcomplete.screens.main.map2.layers.GeometryMarkersLayers
import de.westnordost.streetcomplete.screens.main.map2.layers.PinsLayers
import de.westnordost.streetcomplete.screens.main.map2.layers.SelectedPinsLayer
import de.westnordost.streetcomplete.screens.main.map2.layers.StyleableOverlayLabelLayer
import de.westnordost.streetcomplete.screens.main.map2.layers.StyleableOverlayLayers
import de.westnordost.streetcomplete.screens.main.map2.layers.StyleableOverlaySideLayer
import dev.sargunv.maplibrecompose.compose.CameraState
import dev.sargunv.maplibrecompose.compose.MaplibreMap
import dev.sargunv.maplibrecompose.compose.StyleState
import dev.sargunv.maplibrecompose.compose.rememberCameraState
import dev.sargunv.maplibrecompose.compose.rememberStyleState
import dev.sargunv.maplibrecompose.core.MapOptions
import dev.sargunv.maplibrecompose.core.OrnamentOptions

/**
 * A plain MapLibre Map with StreetComplete theme and localized names
 * */
@Composable
fun Map(
    modifier: Modifier = Modifier,
    cameraState: CameraState = rememberCameraState(),
    styleState: StyleState = rememberStyleState(),
) {
    MaplibreMap(
        modifier = modifier,
        baseStyle = BaseStyle.Json(BASE_STYLE),
        zoomRange = 0f..22f,
        cameraState = cameraState,
        styleState = styleState,
        options = MapOptions(
            ornamentOptions = OrnamentOptions.AllDisabled
        )
    ) {
        val languages = listOf(Locale.current.language)
        val colors = if (isSystemInDarkTheme()) MapColors.Night else MapColors.Light

        MapStyle(
            colors = colors,
            languages = languages,
            belowRoadsContent = {
                // left-and-right lines should be rendered behind the actual road
                StyleableOverlaySideLayer(styleableOverlaySource, isBridge = false)
            },
            belowRoadsOnBridgeContent = {
                // left-and-right lines should be rendered behind the actual bridge road
                StyleableOverlaySideLayer(styleableOverlaySource, isBridge = true)
            },
            belowLabelsContent = {
                // labels should be on top of other layers
                DownloadedAreaLayer(tiles)
                StyleableOverlayLayers(styleableOverlaySource, onClickOverlay)
                TracksLayers()
            },
            aboveLabelsContent = {
                // these are always on top of everything else (including labels)
                StyleableOverlayLabelLayer(styleableOverlaySource, colors.text, colors.textOutline, onClickOverlay)
                GeometryMarkersLayers(markers)
                FocusedGeometryLayers(geometry)
                CurrentLocationLayers(location, rotation)
                PinsLayers(pins, onClickPin, onClickCluster)
                SelectedPinsLayer(iconPainter, pinPositions)
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
      }",
      "layers": []
    }
    """.trimIndent()
