package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pin_circle
import de.westnordost.streetcomplete.screens.main.map.pinPainter
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.screens.main.map.toPinImageBitmap
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.ast.Expression
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
import org.maplibre.compose.expressions.value.ImageValue
import org.maplibre.compose.expressions.value.TranslateAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.DpPadding
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.toJson

private const val CLUSTER_MIN_ZOOM = 13
private const val CLUSTER_MAX_ZOOM = 14
private const val PINS_SOURCE_ID = "pins-source"

/** Displays clustered quest or edit-history pins and handles their feature clicks. */
@Composable
@MaplibreComposable
fun PinsLayers(
    mapState: MapState,
    snapshot: PinSnapshot,
    visible: Boolean = true,
    onClickPin: (properties: Map<String, String>) -> Unit,
    onClickCluster: (leafPositions: List<LatLon>) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val options = remember {
        GeoJsonOptions(
            cluster = true,
            clusterMaxZoom = CLUSTER_MAX_ZOOM,
            clusterRadius = 55,
        )
    }

    val source = remember(options) {
        GeoJsonSource(PINS_SOURCE_ID, EMPTY_PIN_DATA, options)
    }
    val styleLoadState = mapState.style.loadState
    val sourceHandle = mapState.style.sources[PINS_SOURCE_ID] as? GeoJsonSourceHandle
    val generation = remember(styleLoadState) { PinStyleGeneration() }
    val pendingImages = rememberPinStyleImages(
        snapshot.icons,
        generation.installedImageIds,
        styleLoadState == StyleLoadState.Ready && sourceHandle != null,
    )

    LaunchedEffect(sourceHandle, snapshot, pendingImages) {
        if (sourceHandle == null) return@LaunchedEffect
        if (generation.publishedSnapshot === snapshot) return@LaunchedEffect
        try {
            pendingImages.forEach { image ->
                mapState.style.images.add(image.id, image.bitmap)
                generation.installedImageIds += image.id
            }
            sourceHandle.setData(snapshot.data)
            generation.publishedSnapshot = snapshot
        } catch (error: IllegalStateException) {
            // A replacement loaded-style generation will publish a new source handle and retry.
            if (!error.isStyleHandleRace()) throw error
        }
    }

    SymbolLayer(
        id = "pin-cluster-layer",
        source = source,
        filter = all(
            zoom() gte const(CLUSTER_MIN_ZOOM),
            zoom() lte const(CLUSTER_MAX_ZOOM),
            feature["point_count"].convertToNumber() gt const(1),
        ),
        visible = visible,
        iconImage = image(painterResource(Res.drawable.pin_circle)),
        iconSize = const(0.5f) + log2(feature["point_count"].convertToNumber()) / const(10f),
        iconAllowOverlap = const(true),
        iconIgnorePlacement = const(true),
        textField = feature["point_count"].convertToString(),
        textFont = const(listOf("Roboto Regular")),
        textOffset = offset(0.em, 0.1.em),
        textSize = (const(15f) + log2(feature["point_count"].convertToNumber()) / const(1.5f)).sp,
        textAllowOverlap = const(true),
        textIgnorePlacement = const(true),
        onClick = { features ->
            val cluster = features.firstOrNull() ?: return@SymbolLayer ClickResult.Pass
            coroutineScope.launch {
                try {
                    val handle = mapState.awaitGeoJsonSource(PINS_SOURCE_ID)
                    val leaves = handle.getClusterLeaves(cluster, Long.MAX_VALUE, 0L)
                    onClickCluster(leaves.features.mapNotNull { it.geometry.toLatLonOrNull() })
                } catch (error: IllegalStateException) {
                    // Ignore a click racing a base-style replacement.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
            ClickResult.Consume
        },
    )

    CircleLayer(
        id = "pin-dot-layer",
        source = source,
        filter = any(
            zoom() gt const(CLUSTER_MAX_ZOOM),
            all(
                zoom() gte const(CLUSTER_MAX_ZOOM),
                feature["point_count"].convertToNumber() lte const(1),
            ),
        ),
        visible = visible,
        color = const(Color.White),
        strokeColor = const(Color(0xffaaaaaa)),
        radius = const(5.dp),
        strokeWidth = const(1.dp),
        translate = offset(0.dp, (-8).dp),
        translateAnchor = const(TranslateAnchor.Viewport),
    )

    SymbolLayer(
        id = "pins-layer",
        source = source,
        filter = zoom() gt const(CLUSTER_MAX_ZOOM),
        visible = visible,
        sortKey = feature["icon-order"].convertToNumber(),
        iconImage = pinIconExpression(),
        // Dynamic icon sizes and collision handling flicker together, so preserve the fixed size.
        iconSize = const(1f),
        iconPadding = const(
            DpPadding(left = 2.5.dp, top = (-2.5).dp, right = 0.dp, bottom = (-7).dp)
        ),
        iconOffset = const(DpOffset((-4.5).dp, (-34.5).dp)),
        iconAllowOverlap = const(false),
        iconIgnorePlacement = const(false),
        onClick = { features ->
            val properties = features.firstOrNull()?.properties
                ?: return@SymbolLayer ClickResult.Pass
            onClickPin(properties.toStringMap())
            ClickResult.Consume
        },
    )
}

data class Pin(
    val position: LatLon,
    val icon: DrawableResource,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0,
)

/** A pin publication whose revision changes only when its contents change. */
class PinSnapshot private constructor(
    val revision: Long,
    val pins: List<Pin>,
    val icons: List<DrawableResource>,
    val data: GeoJsonData,
) {
    fun updated(pins: List<Pin>): PinSnapshot =
        if (this.pins == pins) this else PinSnapshot(
            revision = revision + 1,
            pins = pins,
            icons = pins.map(Pin::icon).distinct(),
            data = GeoJsonData.JsonString(pinFeatureCollection(pins).toJson()),
        )

    companion object {
        val Empty = PinSnapshot(0, emptyList(), emptyList(), EMPTY_PIN_DATA)
    }
}

internal fun pinIconExpression(): Expression<ImageValue> =
    image(feature["icon-image"].convertToString())

private data class PinStyleImage(val id: String, val bitmap: ImageBitmap)

private class PinStyleGeneration {
    val installedImageIds = mutableSetOf<String>()
    var publishedSnapshot: PinSnapshot? = null
}

@Composable
private fun rememberPinStyleImages(
    resources: List<DrawableResource>,
    installedImageIds: Set<String>,
    styleReady: Boolean,
): List<PinStyleImage> {
    if (!styleReady) return emptyList()
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val result = mutableListOf<PinStyleImage>()
    for (resource in resources) {
        val id = resource.id ?: continue
        if (id in installedImageIds) continue
        result += key(id) {
            val painter = pinPainter(painterResource(resource))
            val bitmap = remember(painter, density, layoutDirection) {
                painter.toPinImageBitmap(density, layoutDirection)
            }
            PinStyleImage(id, bitmap)
        }
    }
    return result
}

private val EMPTY_PIN_DATA = GeoJsonData.Features(pinFeatureCollection(emptyList()))

internal fun pinFeatureCollection(pins: Collection<Pin>): FeatureCollection<Point, JsonObject> =
    FeatureCollection(pins.map(Pin::toGeoJsonFeature))

private fun Pin.toGeoJsonFeature(): Feature<Point, JsonObject> {
    val values = mutableMapOf<String, JsonElement>(
        "icon-image" to JsonPrimitive(icon.id ?: error("Pin icon is not a Compose resource")),
        "icon-order" to JsonPrimitive(order + 50),
    )
    properties.forEach { (key, value) -> values[key] = JsonPrimitive(value) }
    return Feature(Point(position.toPosition()), JsonObject(values))
}

private fun Geometry.toLatLonOrNull(): LatLon? = (this as? Point)?.coordinates?.let {
    LatLon(latitude = it.latitude, longitude = it.longitude)
}

internal fun JsonObject.toStringMap(): Map<String, String> = mapNotNull { (key, value) ->
    val stringValue = (value as? JsonPrimitive)?.contentOrNull ?: return@mapNotNull null
    key to stringValue
}.toMap()

internal fun IllegalStateException.isStyleHandleRace(): Boolean = message in setOf(
    "Style operation belongs to a stale loaded-style identity",
    "Style operation belongs to a stale or unready loaded-style identity",
    "Style operation crossed a loaded-style resource change",
)
