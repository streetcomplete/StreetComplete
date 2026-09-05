package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.SelectedMapPins
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point

private const val SELECTED_PIN_ANIMATION_MILLIS = 300
private const val SELECTED_PINS_SOURCE_ID = "selected-pins-source"
private const val SELECTED_PINS_LAYER_ID = "selected-pins-layer"
private val OvershootEasing = Easing(::overshootInterpolation)

/** Displays highlighted quest pins above the normal quest/edit pin layers. */
@Composable
@MaplibreComposable
internal fun SelectedPinsLayer(
    mapState: MapState,
    selection: SelectedMapPins?,
    imageRegistry: DynamicStyleImageRegistry,
) {
    val source = remember {
        GeoJsonSource(SELECTED_PINS_SOURCE_ID, EMPTY_SELECTED_PIN_DATA, GeoJsonOptions())
    }
    val images = rememberPinStyleImages(selection?.let { listOf(it.icon) }.orEmpty())
    RegisterDynamicStyleImages(imageRegistry, "selected-pins", images)
    val requiredImageIds = images.mapTo(mutableSetOf(), DynamicStyleImage::id)

    LaunchedEffect(mapState, selection) {
        snapshotFlow { mapState.style.loadState }
            .distinctUntilChanged()
            .collectLatest { loadState ->
                if (loadState != StyleLoadState.Ready) return@collectLatest
                val sourceHandle = mapState.style.sources[SELECTED_PINS_SOURCE_ID]
                    as? GeoJsonSourceHandle ?: return@collectLatest
                val layerHandle = mapState.style.layers[SELECTED_PINS_LAYER_ID]
                    ?: return@collectLatest
                try {
                    if (selection == null) {
                        withContext(Dispatchers.Default) {
                            sourceHandle.setData(EMPTY_SELECTED_PIN_DATA)
                        }
                        return@collectLatest
                    }
                    imageRegistry.awaitInstalled(requiredImageIds)
                    val iconId = selection.icon.id
                        ?: error("Selected pin icon is not a Compose resource")
                    val data = GeoJsonData.Features(
                        selectedPinFeatures(selection.positions, iconId)
                    )
                    withContext(Dispatchers.Default) { sourceHandle.setData(data) }
                    // Preserve Compose's frame clock but execute synchronous map-owner calls away
                    // from the UI dispatcher, where another long command would freeze it.
                    withContext(Dispatchers.Default) {
                        animate(
                            initialValue = 0.5f,
                            targetValue = 1.5f,
                            animationSpec = tween(
                                durationMillis = SELECTED_PIN_ANIMATION_MILLIS,
                                easing = OvershootEasing,
                            ),
                        ) { value, _ ->
                            layerHandle.setLayoutProperty("icon-size", JsonPrimitive(value))
                        }
                    }
                } catch (error: IllegalStateException) {
                    // A loaded-style transition changes loadState and retries this effect.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
    }

    SymbolLayer(
        id = SELECTED_PINS_LAYER_ID,
        source = source,
        iconImage = pinIconExpression(),
        iconSize = const(0.5f),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
    )
}

internal fun selectedPinFeatures(
    pinPositions: Collection<LatLon>,
    iconId: String? = null,
): FeatureCollection<Point, JsonObject> = FeatureCollection(
    pinPositions.map { position ->
        Feature(
            Point(position.toPosition()),
            JsonObject(iconId?.let { mapOf("icon-image" to JsonPrimitive(it)) }.orEmpty()),
        )
    }
)

private val EMPTY_SELECTED_PIN_DATA = GeoJsonData.Features(selectedPinFeatures(emptyList()))

internal fun overshootInterpolation(fraction: Float, tension: Float = 2f): Float {
    val shifted = fraction - 1f
    return shifted * shifted * ((tension + 1f) * shifted + tension) + 1f
}
