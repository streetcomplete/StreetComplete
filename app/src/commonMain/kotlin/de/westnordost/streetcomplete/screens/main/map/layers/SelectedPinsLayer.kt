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
import de.westnordost.streetcomplete.screens.main.map.pinPainter
import de.westnordost.streetcomplete.screens.main.map.toPosition
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point

private const val SELECTED_PIN_ANIMATION_MILLIS = 300
private val OvershootEasing = Easing(::overshootInterpolation)

/** Displays highlighted quest pins above the normal quest/edit pin layers. */
@Composable
@MaplibreComposable
fun SelectedPinsLayer(icon: DrawableResource, pinPositions: Collection<LatLon>) {
    val iconSize = remember { Animatable(0.5f) }
    LaunchedEffect(icon, pinPositions) {
        iconSize.snapTo(0.5f)
        iconSize.animateTo(
            targetValue = 1.5f,
            animationSpec = tween(
                durationMillis = SELECTED_PIN_ANIMATION_MILLIS,
                easing = OvershootEasing,
            ),
        )
    }

    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(selectedPinFeatures(pinPositions))
    )

    SymbolLayer(
        id = "selected-pins-layer",
        source = source,
        iconImage = image(pinPainter(painterResource(icon))),
        iconSize = const(iconSize.value),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
    )
}

internal fun selectedPinFeatures(
    pinPositions: Collection<LatLon>
): FeatureCollection<Point, JsonObject> = FeatureCollection(
    pinPositions.map { position ->
        Feature(Point(position.toPosition()), JsonObject(emptyMap()))
    }
)

internal fun overshootInterpolation(fraction: Float, tension: Float = 2f): Float {
    val shifted = fraction - 1f
    return shifted * shifted * ((tension + 1f) * shifted + tension) + 1f
}
