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
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.compose.source.rememberGeoJsonSource
import dev.sargunv.maplibrecompose.core.source.GeoJsonData
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.image
import io.github.dellisd.spatialk.geojson.FeatureCollection
import kotlinx.serialization.json.JsonPrimitive

/** Displays "selected" pins. Those pins should always be shown on top of pins displayed by
 *  [PinsLayers] */
@MaplibreComposable @Composable
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
        id = "selected-pins-source",
        data = GeoJsonData.Features(
            FeatureCollection(pinPositions.map {
                io.github.dellisd.spatialk.geojson.Feature(
                    geometry = it.toGeometry(),
                    properties = mapOf("icon-image" to JsonPrimitive(icon))
                )
            })
        ),
    )

    SymbolLayer(
        id = "selected-pins-layer",
        source = source,
        iconImage = image(Feature.get("icon-image")), // TODO
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
