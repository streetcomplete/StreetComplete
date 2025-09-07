package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.map_pin_circle
import de.westnordost.streetcomplete.screens.main.map2.toGeometry
import io.github.dellisd.spatialk.geojson.Feature
import io.github.dellisd.spatialk.geojson.FeatureCollection
import kotlinx.serialization.json.JsonPrimitive
import org.jetbrains.compose.resources.painterResource
import org.maplibre.android.style.expressions.Expression.log2
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.any
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.div
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.gt
import org.maplibre.compose.expressions.dsl.gte
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.log2
import org.maplibre.compose.expressions.dsl.lte
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.dsl.sp
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.TranslateAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.FeaturesClickHandler
import org.maplibre.compose.util.MaplibreComposable

/** Display pins on the map, e.g. quest pins or pins for recent edits */
@MaplibreComposable
@Composable
fun PinsLayers(
    pins: Collection<Pin>,
    onClickPin: FeaturesClickHandler? = null,
    onClickCluster: FeaturesClickHandler? = null,
) {
    // TODO is this recomposed all the time? In that case, remember the features
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(pins.map { it.toGeoJsonFeature() })),
        options = GeoJsonOptions(
            cluster = true,
            clusterMaxZoom = CLUSTER_MAX_ZOOM,
            clusterRadius = 55
        )
    )

    SymbolLayer(
        id = "pin-cluster-layer",
        source = source,
        minZoom = CLUSTER_MIN_ZOOM.toFloat(),
        maxZoom = CLUSTER_MAX_ZOOM.toFloat(),
        filter = all(
            zoom() gte const(CLUSTER_MIN_ZOOM),
            zoom() lte const(CLUSTER_MAX_ZOOM),
            feature["point_count"].convertToNumber() gt const(1)
        ),
        iconImage = image(painterResource(Res.drawable.map_pin_circle)),
        iconSize = const(0.5f) + (log2(feature["point_count"].convertToNumber()) / const(10f)),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        textField = feature["point_count"].convertToString(),
        textSize = (const(15f) + (log2(feature["point_count"].convertToNumber()) / const(1.5f))).sp,
        textFont = const(listOf("Roboto Regular")),
        textOffset = offset(0.em, 0.1.em),
        textAllowOverlap = const(true),
        textIgnorePlacement = const(true),
        onClick = onClickCluster,
    )
    CircleLayer(
        id = "pin-dot-layer",
        source = source,
        minZoom = CLUSTER_MIN_ZOOM.toFloat(),
        filter = any(
            zoom() gt const(CLUSTER_MAX_ZOOM),
            all(
                zoom() gte const(CLUSTER_MIN_ZOOM),
                feature["point_count"].convertToNumber() lte const(1)
            )
        ),
        color = const(Color.White),
        radius = const(5.dp),
        strokeColor = const(Color(0xffaaaaaa)),
        strokeWidth = const(1.dp),
        translate = offset(0.dp, -8.dp), // so that it hides behind the pin
        translateAnchor = const(TranslateAnchor.Viewport),
    )
    SymbolLayer(
        id = "pins-layer",
        source = source,
        minZoom = CLUSTER_MAX_ZOOM.toFloat(),
        filter = zoom() gt const(CLUSTER_MAX_ZOOM),
        sortKey = feature["icon-order"].convertToNumber(),
        iconImage = image(feature["icon-image"]), // TODO
        // constant icon size because click area would become a bit too small and more
        // importantly, dynamic size per zoom + collision doesn't work together well, it
        // results in a lot of flickering.
        iconSize = const(1f),
        iconPadding = const(PaddingValues.Absolute(
            left = 2.5.dp,
            top = -2.5.dp,
            right = 0.dp,
            bottom = -7.dp,
        )),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
        iconAllowOverlap = const(false),
        iconIgnorePlacement = const(false),
        onClick = onClickPin,
    )
}

private const val CLUSTER_MIN_ZOOM = 13
private const val CLUSTER_MAX_ZOOM = 14

data class Pin(
    val position: LatLon,
    val icon: String,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0
)

private fun Pin.toGeoJsonFeature() =
    Feature(
        geometry = position.toGeometry(),
        properties = mapOf(
            "icon-image" to JsonPrimitive(icon),
            "icon-order" to JsonPrimitive(order + 50),
        ) + properties.map { it.first to JsonPrimitive(it.second) }
    )
