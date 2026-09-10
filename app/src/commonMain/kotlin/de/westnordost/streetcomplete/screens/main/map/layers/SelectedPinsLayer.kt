package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map.SelectedMapPins
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point

private const val SELECTED_PIN_ANIMATION_MILLIS = 300
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
    val images = rememberPinStyleImages(selection?.let { listOf(it.icon) }.orEmpty())
    RegisterDynamicStyleImages(imageRegistry, "selected-pins", images)
    val data =
        remember(selection) {
            if (selection == null) {
                EMPTY_SELECTED_PIN_DATA
            } else {
                val iconId =
                    selection.icon.id ?: error("Selected pin icon is not a Compose resource")
                GeoJsonData.Features(selectedPinFeatures(selection.positions, iconId))
            }
        }
    val source =
        rememberImageBackedGeoJsonSource(
            mapState = mapState,
            data = data,
            imageRegistry = imageRegistry,
            requiredImageIds = images.mapTo(mutableSetOf(), DynamicStyleImage::id),
        )
    val iconSize = remember { Animatable(0.5f) }
    LaunchedEffect(selection) {
        iconSize.snapTo(0.5f)
        if (selection != null) {
            iconSize.animateTo(
                targetValue = 1.5f,
                animationSpec =
                    tween(
                        durationMillis = SELECTED_PIN_ANIMATION_MILLIS,
                        easing = OvershootEasing,
                    ),
            )
        }
    }

    SymbolLayer(
        id = SELECTED_PINS_LAYER_ID,
        source = source,
        iconImage = pinIconExpression(),
        iconSize = const(iconSize.value),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
    )
}

internal fun selectedPinFeatures(
    pinPositions: Collection<LatLon>,
    iconId: String? = null,
): FeatureCollection<Point, JsonObject> =
    FeatureCollection(
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
