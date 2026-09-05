package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.CameraConstraints
import org.maplibre.compose.map.DefaultMapRuntime
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.RenderOptions
import org.maplibre.compose.map.TileLodOptions
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.overlay.include
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.util.MapClickHandler
import org.maplibre.compose.util.MaplibreComposable

/**
 * Displays a StreetComplete map state with the app's camera limits and platform lifecycle policy.
 * The durable base style and declarative layers are owned by [state].
 */
@Composable
fun StreetCompleteMap(
    state: MapState,
    modifier: Modifier = Modifier,
    cameraPadding: PaddingValues = PaddingValues(0.dp),
    cameraConstraints: CameraConstraints = CameraConstraints(maxZoom = 22.0),
    renderOptions: RenderOptions = RenderOptions.Standard,
    gestureOptions: GestureOptions = GestureOptions.Standard,
    tileLodOptions: TileLodOptions = TileLodOptions.Standard,
    onClick: MapClickHandler,
    onLongClick: MapClickHandler,
    overlay: MapOverlay = MapOverlay {},
) {
    MaplibreMap(
        state = state,
        modifier = modifier,
        cameraPadding = cameraPadding,
        cameraConstraints = cameraConstraints,
        renderOptions = renderOptions,
        gestureOptions = gestureOptions,
        tileLodOptions = tileLodOptions,
        onClick = onClick,
        onLongClick = onLongClick,
    ) {
        include(overlay)
    }
}

/** Remembers the logical map state used by [StreetCompleteMap]. */
@Composable
fun rememberStreetCompleteMapState(
    initialCameraPosition: CameraPosition = CameraPosition(),
    runtime: MapRuntime = DefaultMapRuntime.instance,
    content: @Composable @MaplibreComposable () -> Unit = {},
): MapState = rememberMapState(
    runtime = runtime,
    baseStyle = BaseStyle.Json(streetCompleteBaseStyle()),
    initialCameraPosition = initialCameraPosition,
    content = content,
)

// Compose resources exposes the local glyph directory differently on each target. Build the URI
// from one concrete resource so that MapLibre can substitute the requested stack and range.
internal fun streetCompleteBaseStyle(): String = """
    {
      "version": 8,
      "name": "StreetComplete",
      "metadata": {},
      "sources": {},
      "glyphs": "${streetCompleteGlyphTemplate(
        Res.getUri("files/glyphs/Roboto Regular/0-255.pbf")
      )}",
      "layers": []
    }
""".trimIndent()

internal fun streetCompleteGlyphTemplate(resourceUri: String): String = resourceUri
    // URL.toURI and NSURL encode spaces; Android's asset URI currently does not.
    .replace("Roboto%20Regular", "{fontstack}")
    .replace("Roboto Regular", "{fontstack}")
    .replace("0-255", "{range}")
