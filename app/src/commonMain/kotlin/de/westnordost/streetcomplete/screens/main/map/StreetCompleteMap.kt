package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.resources.Res
import org.maplibre.compose.camera.CameraPosition
import org.maplibre.compose.map.MapPresentationCallbacks
import org.maplibre.compose.map.MapPresentationOptions
import org.maplibre.compose.map.MapRuntime
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.rememberMapState
import org.maplibre.compose.map.rememberMapRuntime
import org.maplibre.compose.overlay.MapOverlay
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.StyleComposition
import org.maplibre.compose.util.MaplibreComposable

/**
 * A MapLibre Compose map with StreetComplete's complete light or night base-map style.
 *
 * The four content slots preserve the intentional ordering seams of the Android style. App-owned
 * geometry can be inserted below roads, below bridge roads, below labels, or above labels without
 * coupling this base map to quest, edit-history, location, or overlay state.
 */
@Composable
fun StreetCompleteMap(
    state: MapState,
    modifier: Modifier = Modifier,
    presentationOptions: MapPresentationOptions = MapPresentationOptions(zoomRange = 0f..22f),
    callbacks: MapPresentationCallbacks = MapPresentationCallbacks(),
    overlay: MapOverlay = MapOverlay.None,
    hiddenBaseLayerIds: Set<String> = emptySet(),
    belowRoadsContent: @Composable @MaplibreComposable () -> Unit = {},
    belowRoadsOnBridgeContent: @Composable @MaplibreComposable () -> Unit = {},
    belowLabelsContent: @Composable @MaplibreComposable () -> Unit = {},
    aboveLabelsContent: @Composable @MaplibreComposable () -> Unit = {},
) {
    // TODO(maplibre-compose): Restore StreetComplete's 300ms, system-scale-aware global style
    // transition when MapLibre Compose exposes style transition configuration in common code.
    val colors = if (MaterialTheme.colors.isLight) MapColors.Light else MapColors.Night
    val languages = listOf(Locale.current.language)
    val styleComposition = remember(
        colors,
        languages,
        belowRoadsContent,
        belowRoadsOnBridgeContent,
        belowLabelsContent,
        aboveLabelsContent,
        hiddenBaseLayerIds,
    ) {
        StyleComposition {
            MapStyle(
                colors = colors,
                languages = languages,
                belowRoadsContent = belowRoadsContent,
                belowRoadsOnBridgeContent = belowRoadsOnBridgeContent,
                belowLabelsContent = belowLabelsContent,
                aboveLabelsContent = aboveLabelsContent,
                hiddenBaseLayerIds = hiddenBaseLayerIds,
            )
        }
    }

    MaplibreMap(
        state = state,
        styleComposition = styleComposition,
        modifier = modifier,
        presentationOptions = presentationOptions,
        callbacks = callbacks,
        overlay = overlay,
    )
}

/** Remembers the logical map state used by [StreetCompleteMap]. */
@Composable
fun rememberStreetCompleteMapState(
    initialCameraPosition: CameraPosition = CameraPosition(),
    runtime: MapRuntime = rememberMapRuntime(),
): MapState = rememberMapState(
    runtime = runtime,
    initialCameraPosition = initialCameraPosition,
    initialBaseStyle = BaseStyle.Json(streetCompleteBaseStyle()),
)

// Compose resources exposes the local glyph directory differently on each target. Build the URI
// from one concrete resource so that MapLibre can substitute the requested stack and range.
private fun streetCompleteBaseStyle(): String = """
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
