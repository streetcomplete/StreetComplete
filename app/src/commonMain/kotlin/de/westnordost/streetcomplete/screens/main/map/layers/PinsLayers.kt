package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.map_pin_circle
import de.westnordost.streetcomplete.screens.main.map.pinPainter
import de.westnordost.streetcomplete.screens.main.map.toGeoJsonBoundingBox
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.any
import org.maplibre.compose.expressions.dsl.case
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.div
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.gt
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.log2
import org.maplibre.compose.expressions.dsl.lte
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.plus
import org.maplibre.compose.expressions.dsl.sp
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.textOffset
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.TranslateAnchor
import org.maplibre.compose.interaction.ClickResult
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.LocalMapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.StyleHandleException
import org.maplibre.compose.util.DpPadding
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import kotlin.time.Duration.Companion.milliseconds

/** Display pins on the map, e.g. quest pins or pins for recent edits */
@MaplibreComposable
@Composable
fun PinsLayers(
    pins: Collection<Pin>,
    onClickPin: (properties: JsonObject) -> ClickResult,
) {
    val mapState = checkNotNull(LocalMapState.current)
    val coroutineScope = rememberCoroutineScope()

    val features by produceState<List<Feature<Point, JsonObject>>>(emptyList(), pins) {
        value = withContext(Dispatchers.Default) { pins.map { it.toGeoJsonFeature() } }
    }
    val options = remember {
        GeoJsonOptions(
            cluster = true,
            clusterMaxZoom = CLUSTER_MAX_ZOOM,
            clusterRadius = 55,
        )
    }

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(FeatureCollection(features)),
        options = options
    )

    val handle = mapState.style.sources[source]
    var clusterJob by remember { mutableStateOf<Job?>(null) }
    DisposableEffect(handle, features) {
        onDispose { clusterJob?.cancel() }
    }

    fun onClickCluster(features: List<Feature<Geometry, JsonObject?>>): ClickResult {
        val feature = features.firstOrNull() ?: return ClickResult.Pass
        val currentHandle = handle ?: return ClickResult.Pass
        clusterJob?.cancel()
        clusterJob = coroutineScope.launch {
            val leaves = try {
                currentHandle.getClusterLeaves(feature, Long.MAX_VALUE, 0)
            } catch (e: StyleHandleException) {
                if (mapState.style.sources[source] !== currentHandle) return@launch
                throw e
            }
            val positions = leaves.features.mapNotNull { (it.geometry as? Point)?.coordinates }
                .map { LatLon(it.latitude, it.longitude) }
            if (positions.isEmpty()) return@launch
            val bounds = positions.enclosingBoundingBox()
            val camera = mapState.cameraPosition
            mapState.animateCameraToBounds(
                bounds.toGeoJsonBoundingBox(),
                bearing = camera.bearing,
                tilt = camera.tilt,
                duration = 450.milliseconds,
            )
        }
        return ClickResult.Consume
    }

    fun onClick(features: List<Feature<Geometry, JsonObject?>>): ClickResult {
        val properties = features.firstOrNull()?.properties ?: return ClickResult.Pass
        return onClickPin(properties)
    }

    SymbolLayer(
        id = "pin-cluster-layer",
        source = source,
        minZoom = CLUSTER_MIN_ZOOM.toFloat(),
        filter = all(
            zoom().lte(const(CLUSTER_MAX_ZOOM)),
            feature["point_count"].convertToNumber().gt(const(1)),
        ),
        iconImage = image(painterResource(Res.drawable.map_pin_circle)),
        iconSize = const(0.5f) + (log2(feature["point_count"].convertToNumber()) / const(10f)),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        textField = feature["point_count"].convertToString(),
        textSize = (const(15f) + log2(feature["point_count"].convertToNumber()) / const(1.5f)).sp,
        textFont = const(listOf("Roboto Regular")),
        textOffset = textOffset(0.em, 0.1.em),
        textAllowOverlap = const(true),
        textIgnorePlacement = const(true),
        onClick = ::onClickCluster,
    )
    CircleLayer(
        id = "pin-dot-layer",
        source = source,
        minZoom = CLUSTER_MAX_ZOOM.toFloat(),
        filter = any(
            zoom().gt(const(CLUSTER_MAX_ZOOM)),
            feature["point_count"].convertToNumber(const(1)).lte(const(1)),
        ),
        color = const(Color.White),
        radius = const(5.dp),
        strokeColor = const(Color(0xffaaaaaa)),
        strokeWidth = const(1.dp),
        translate = offset(0.dp, -8.dp), // so that it hides behind the pin
        translateAnchor = const(TranslateAnchor.Viewport),
    )
    val pinImages = pins.map { it.icon }.distinct().map { icon ->
        case("pin_" + icon.id, image(pinPainter(painterResource(icon)), size = DpSize(71.dp, 71.dp)))
    }
    SymbolLayer(
        id = "pins-layer",
        source = source,
        filter = zoom().gt(const(CLUSTER_MAX_ZOOM)),
        sortKey = feature["icon-order"].convertToNumber(),
        iconImage = switch(feature["icon-image"].convertToString(), pinImages, image("")),
        // constant icon size because click area would become a bit too small and more
        // importantly, dynamic size per zoom + collision doesn't work together well, it
        // results in a lot of flickering.
        iconSize = const(1f),
        iconPadding = const(DpPadding(
            left = 2.5.dp,
            top = -2.5.dp,
            right = 0.dp,
            bottom = -7.dp,
        )),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
        iconAllowOverlap = const(false),
        iconIgnorePlacement = const(false),
        onClick = ::onClick,
    )
}

private const val CLUSTER_MIN_ZOOM = 13
private const val CLUSTER_MAX_ZOOM = 14
