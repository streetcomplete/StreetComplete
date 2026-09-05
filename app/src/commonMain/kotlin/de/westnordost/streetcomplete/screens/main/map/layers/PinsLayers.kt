package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pin_circle
import de.westnordost.streetcomplete.screens.main.map.pinPainter
import de.westnordost.streetcomplete.screens.main.map.toPosition
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.DpPadding
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.Point

private const val CLUSTER_MIN_ZOOM = 13
private const val CLUSTER_MAX_ZOOM = 14
internal const val PINS_SOURCE_ID = "pins-source"
internal val PIN_LAYER_IDS = listOf("pin-cluster-layer", "pin-dot-layer", "pins-layer")

/** Displays clustered quest or edit-history pins and handles their feature clicks. */
@Composable
@MaplibreComposable
internal fun PinsLayers(
    mapState: MapState,
    snapshot: PinSnapshot,
    visible: Boolean,
    imageRegistry: DynamicStyleImageRegistry,
    onClickPin: (properties: Map<String, String>) -> Unit,
    onClickCluster: (leafPositions: List<LatLon>) -> Unit,
) {
    ImperativeLayerVisibility(mapState, PIN_LAYER_IDS, visible)
    val coroutineScope = rememberCoroutineScope()
    val currentOnClickPin = rememberUpdatedState(onClickPin)
    val currentOnClickCluster = rememberUpdatedState(onClickCluster)
    val clusterClickHandler = remember(mapState, coroutineScope) {
        clusterClickHandler@{ features: List<Feature<Geometry, JsonObject?>> ->
            val cluster = features.firstOrNull()
                ?: return@clusterClickHandler ClickResult.Pass
            coroutineScope.launch {
                try {
                    val handle = mapState.awaitGeoJsonSource(PINS_SOURCE_ID)
                    val leaves = handle.getClusterLeaves(cluster, Long.MAX_VALUE, 0L)
                    currentOnClickCluster.value(
                        leaves.features.mapNotNull { it.geometry.toLatLonOrNull() }
                    )
                } catch (error: IllegalStateException) {
                    // Ignore a click racing a base-style replacement.
                    if (!error.isStyleHandleRace()) throw error
                }
            }
            ClickResult.Consume
        }
    }
    val pinClickHandler = remember {
        pinClickHandler@{ features: List<Feature<Geometry, JsonObject?>> ->
            val properties = features.firstOrNull()?.properties
                ?: return@pinClickHandler ClickResult.Pass
            currentOnClickPin.value(properties.toStringMap())
            ClickResult.Consume
        }
    }
    val options = remember {
        GeoJsonOptions(
            cluster = true,
            clusterMaxZoom = CLUSTER_MAX_ZOOM,
            clusterRadius = 55,
        )
    }

    val images = rememberPinStyleImages(snapshot.icons)
    RegisterDynamicStyleImages(imageRegistry, "quest-pins", images)
    val requiredImageIds = images.mapTo(mutableSetOf(), DynamicStyleImage::id)
    val source = rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = PINS_SOURCE_ID,
        data = snapshot.data,
        options = options,
        imageRegistry = imageRegistry,
        requiredImageIds = requiredImageIds,
    )

    SymbolLayer(
        id = "pin-cluster-layer",
        source = source,
        filter = all(
            zoom() gte const(CLUSTER_MIN_ZOOM),
            zoom() lte const(CLUSTER_MAX_ZOOM),
            feature["point_count"].convertToNumber() gt const(1),
        ),
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
        onClick = clusterClickHandler,
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
        onClick = pinClickHandler,
    )
}

data class Pin(
    val position: LatLon,
    val icon: DrawableResource,
    val properties: Collection<Pair<String, String>> = emptyList(),
    val order: Int = 0,
)

/** Reuses prepared pin data until its contents change. */
class PinSnapshot private constructor(
    val pins: List<Pin>,
    val icons: List<DrawableResource>,
    val data: GeoJsonData,
) {
    fun updated(pins: List<Pin>): PinSnapshot =
        if (this.pins == pins) this else PinSnapshot(
            pins = pins,
            icons = pins.map(Pin::icon).distinct(),
            data = GeoJsonData.JsonString(pinGeoJson(pins)),
        )

    companion object {
        val Empty = PinSnapshot(emptyList(), emptyList(), EMPTY_PIN_DATA)
    }
}

internal fun pinIconExpression(): Expression<ImageValue> =
    image(feature["icon-image"].convertToString())

@Composable
internal fun rememberPinStyleImages(
    resources: List<DrawableResource>,
): List<DynamicStyleImage> {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val result = mutableListOf<DynamicStyleImage>()
    for (resource in resources) {
        val id = resource.id ?: continue
        result += key(id) {
            val painter = pinPainter(painterResource(resource))
            DynamicStyleImage(
                id = id,
                painter = painter,
                density = density,
                layoutDirection = layoutDirection,
                size = DpSize(71.dp, 71.dp),
                cacheKey = listOf("pin", id, density.density, density.fontScale, layoutDirection),
            )
        }
    }
    return result
}

private val EMPTY_PIN_DATA = GeoJsonData.JsonString(pinGeoJson(emptyList()))

/** Serializes the hot pin publication path without first allocating a complete GeoJSON tree. */
internal fun pinGeoJson(pins: Collection<Pin>): String = buildString {
    append("{\"type\":\"FeatureCollection\",\"features\":[")
    pins.forEachIndexed { index, pin ->
        if (index > 0) append(',')
        append("{\"type\":\"Feature\",\"geometry\":{\"type\":\"Point\",\"coordinates\":[")
        append(pin.position.longitude)
        append(',')
        append(pin.position.latitude)
        append("]},\"properties\":{")

        appendJsonProperty(
            "icon-image",
            pin.properties.lastValueOf("icon-image")
                ?: pin.icon.id
                ?: error("Pin icon is not a Compose resource"),
        )
        append(',')
        val overriddenOrder = pin.properties.lastValueOf("icon-order")
        appendJsonString("icon-order")
        append(':')
        if (overriddenOrder == null) append(pin.order + 50) else appendJsonString(overriddenOrder)

        pin.properties.forEachIndexed propertiesLoop@ { propertyIndex, (key, value) ->
            if (key == "icon-image" || key == "icon-order") return@propertiesLoop
            if (pin.properties.hasKeyAfter(key, propertyIndex)) return@propertiesLoop
            append(',')
            appendJsonProperty(key, value)
        }
        append("}}")
    }
    append("]}")
}

private fun Collection<Pair<String, String>>.lastValueOf(key: String): String? {
    var result: String? = null
    for ((candidate, value) in this) if (candidate == key) result = value
    return result
}

private fun Collection<Pair<String, String>>.hasKeyAfter(key: String, index: Int): Boolean {
    var candidateIndex = 0
    for ((candidate, _) in this) {
        if (candidateIndex++ > index && candidate == key) return true
    }
    return false
}

private fun StringBuilder.appendJsonProperty(key: String, value: String) {
    appendJsonString(key)
    append(':')
    appendJsonString(value)
}

private fun StringBuilder.appendJsonString(value: String) {
    append(Json.encodeToString(value))
}

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
    "No ready loaded style",
    "Style operation belongs to a stale loaded-style identity",
    "Style operation belongs to a stale or unready loaded-style identity",
    "Style operation crossed a loaded-style resource change",
)
