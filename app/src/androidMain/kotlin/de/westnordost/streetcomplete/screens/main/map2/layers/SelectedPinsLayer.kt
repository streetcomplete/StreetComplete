package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.screens.main.map2.toGeometry
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import kotlinx.serialization.json.JsonPrimitive
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable

/** Displays "selected" pins. Those pins should always be shown on top of pins displayed by
 *  [PinsLayers] */
@MaplibreComposable
@Composable
fun SelectedPinsLayer(icon: String, pinPositions: Collection<LatLon>) {
    val pinsSize = remember { Animatable(0.5f) }
    LaunchedEffect(pinPositions) {
        pinsSize.animateTo(
            targetValue = 1.5f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            ),
        )
    }

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(
            FeatureCollection(pinPositions.map {
                Feature(
                    geometry = it.toGeometry(),
                    properties = mapOf("icon-image" to JsonPrimitive(icon))
                )
            })
        ),
    )

    SymbolLayer(
        id = "selected-pins-layer",
        source = source,
        iconImage = image(feature["icon-image"]), // TODO
        iconSize = const(pinsSize.value),
        iconPadding = const(PaddingValues.Absolute(
            left = 2.5.dp,
            top = -2.5.dp,
            right = 0.dp,
            bottom = -7.dp,
        )),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
    )
}
