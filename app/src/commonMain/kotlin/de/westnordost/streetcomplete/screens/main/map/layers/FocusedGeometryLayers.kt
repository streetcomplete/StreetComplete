package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.minus
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.dsl.times
import org.maplibre.compose.expressions.value.FloatValue
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import kotlin.math.PI
import kotlin.math.sin

private const val HIGHLIGHT_CYCLE_MILLIS = 1200
private const val FOCUSED_GEOMETRY_SOURCE_ID = "focus-geometry-source"
private const val FOCUSED_GEOMETRY_FEATURE_ID = "1"
private val HighlightColor = Color(0xffd14000)

/** Displays the pulsing point, line, or polygon geometry of the currently focused element. */
@Composable
@MaplibreComposable
fun FocusedGeometryLayers(mapState: MapState, geometry: ElementGeometry?) {
    val source = remember {
        GeoJsonSource(FOCUSED_GEOMETRY_SOURCE_ID, EMPTY_FOCUSED_GEOMETRY_DATA, GeoJsonOptions())
    }
    val currentGeometry = rememberUpdatedState(geometry)

    LaunchedEffect(mapState) {
        snapshotFlow {
            Triple(
                mapState.style.loadState,
                mapState.style.sources[FOCUSED_GEOMETRY_SOURCE_ID] != null,
                currentGeometry.value,
            )
        }
            .distinctUntilChanged()
            .collectLatest { (loadState, sourceInstalled, focusedGeometry) ->
                if (loadState != StyleLoadState.Ready || !sourceInstalled) {
                    return@collectLatest
                }
                val sourceHandle = mapState.style.sources[FOCUSED_GEOMETRY_SOURCE_ID]
                    as? GeoJsonSourceHandle ?: return@collectLatest
                try {
                    withContext(Dispatchers.Default) {
                        sourceHandle.setData(
                            focusedGeometry?.let(::focusedGeometryData)
                                ?: EMPTY_FOCUSED_GEOMETRY_DATA
                        )
                    }
                    if (focusedGeometry == null) return@collectLatest

                    val animationStartedAt = withFrameNanos { it }
                    while (true) {
                        val frameTime = withFrameNanos { it }
                        val elapsedNanos = (frameTime - animationStartedAt).coerceAtLeast(0L)
                        val cycleFraction =
                            (elapsedNanos % HIGHLIGHT_CYCLE_NANOS).toFloat() /
                                HIGHLIGHT_CYCLE_NANOS.toFloat()
                        // The native command waits for MapLibre's owner thread. Keep that wait away
                        // from Compose's UI dispatcher while style images are being installed.
                        withContext(Dispatchers.Default) {
                            sourceHandle.setFeatureState(
                                FOCUSED_GEOMETRY_FEATURE_ID,
                                buildJsonObject {
                                    put("breathing", focusedGeometryBreathing(cycleFraction))
                                },
                            )
                        }
                    }
                } catch (error: IllegalStateException) {
                    // A loaded-style transition changes the observed state and retries this effect.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }

    val breathing = feature.state<FloatValue>("breathing").asNumber(const(0f))
    val sizeFactor = breathing + const(0.75f)
    val opacity = (const(1f) - breathing) * const(0.5f) + const(0.15f)

    FillLayer(
        id = "focus-geo-fill",
        source = source,
        filter = feature.isArea(),
        opacity = const(0.3f),
        color = const(HighlightColor),
    )
    LineLayer(
        id = "focus-geo-lines",
        source = source,
        filter = !feature.isPoint(),
        opacity = opacity,
        color = const(HighlightColor),
        width = const(10.dp) * sizeFactor,
        cap = const(LineCap.Round),
    )
    CircleLayer(
        id = "focus-geo-circle",
        source = source,
        filter = feature.isPoint(),
        opacity = opacity,
        color = const(HighlightColor),
        radius = const(12.dp) * sizeFactor,
    )
}

internal data class FocusedGeometryStyle(
    val opacity: Float,
    val lineWidth: Float,
    val circleRadius: Float,
)

internal fun focusedGeometryStyle(cycleFraction: Float): FocusedGeometryStyle {
    val breathing = focusedGeometryBreathing(cycleFraction)
    val sizeFactor = breathing + 0.75f
    return FocusedGeometryStyle(
        opacity = (1f - breathing) * 0.5f + 0.15f,
        lineWidth = 10f * sizeFactor,
        circleRadius = 12f * sizeFactor,
    )
}

private fun focusedGeometryBreathing(cycleFraction: Float): Float =
    sin(cycleFraction * 2f * PI.toFloat()) / 2f + 0.5f

private fun focusedGeometryData(geometry: ElementGeometry): GeoJsonData = GeoJsonData.Features(
    FeatureCollection(
        listOf(
            Feature<Geometry, JsonObject>(
                geometry = geometry.toGeometry(),
                properties = JsonObject(emptyMap()),
                id = JsonPrimitive(FOCUSED_GEOMETRY_FEATURE_ID),
            )
        )
    )
)

private val EMPTY_FOCUSED_GEOMETRY_DATA = GeoJsonData.Features(
    FeatureCollection<Geometry, JsonObject>(emptyList())
)

private const val HIGHLIGHT_CYCLE_NANOS = HIGHLIGHT_CYCLE_MILLIS * 1_000_000L
