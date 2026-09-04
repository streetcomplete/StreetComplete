package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.data.osm.mapdata.ElementKey
import de.westnordost.streetcomplete.screens.main.map.byZoom
import de.westnordost.streetcomplete.screens.main.map.inMeters
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isLines
import de.westnordost.streetcomplete.screens.main.map.isPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.DrawableResource
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.expressions.value.SymbolZOrder
import org.maplibre.compose.layers.FillExtrusionLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.MapState
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.GeoJsonOptions
import org.maplibre.compose.sources.GeoJsonSource
import org.maplibre.compose.sources.Source
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry
import org.maplibre.spatialk.geojson.toJson

private const val OVERLAY_SOURCE_ID = "overlay-source"
private const val MIN_ZOOM = 14
internal val STYLEABLE_OVERLAY_LAYER_IDS = listOf(
    "overlay-lines-side",
    "overlay-lines-dashed-side",
    "overlay-lines-bridge-side",
    "overlay-lines-dashed-bridge-side",
    "overlay-lines-casing",
    "overlay-fills",
    "overlay-lines",
    "overlay-lines-dashed",
    "overlay-fills-outline",
    "overlay-heights",
    "overlay-symbols",
)

/** Creates the one source shared across the overlay's four map-style insertion points. */
@Composable
internal fun rememberStyleableOverlaySource(
    mapState: MapState,
    styledElements: Collection<StyledElement>,
    imageRegistry: DynamicStyleImageRegistry,
): GeoJsonSource {
    val options = remember { GeoJsonOptions(minZoom = MIN_ZOOM) }
    val prepared by produceState(PREPARED_EMPTY_OVERLAY, styledElements) {
        value = withContext(Dispatchers.Default) {
            PreparedOverlay(
                data = GeoJsonData.JsonString(
                    FeatureCollection(
                        styledElements.flatMap(StyledElement::toOverlayFeatures)
                    ).toJson()
                ),
                resources = styledElements.mapNotNull(StyledElement::overlayIcon).distinct(),
            )
        }
    }
    val images = rememberPlainStyleImages(prepared.resources)
    RegisterDynamicStyleImages(imageRegistry, "styleable-overlay", images)
    val requiredImageIds = images.mapTo(mutableSetOf(), DynamicStyleImage::id)
    return rememberImperativeGeoJsonSource(
        mapState = mapState,
        id = OVERLAY_SOURCE_ID,
        data = prepared.data,
        options = options,
        imageRegistry = imageRegistry,
        requiredImageIds = requiredImageIds,
    )
}

private data class PreparedOverlay(
    val data: GeoJsonData,
    val resources: List<DrawableResource>,
)

private val PREPARED_EMPTY_OVERLAY = PreparedOverlay(
    data = GeoJsonData.Features(FeatureCollection<Geometry, JsonObject>(emptyList())),
    resources = emptyList(),
)

/** Draws left/right road strokes below ordinary or bridge roads. */
@Composable
@MaplibreComposable
fun StyleableOverlaySideLayers(
    source: Source,
    bridge: Boolean,
) {
    val bridgeFilter = if (bridge) feature.has(BRIDGE) else !feature.has(BRIDGE)
    val commonFilter = all(feature.isLines(), feature.has(OFFSET), bridgeFilter)
    val color = feature[COLOR].convertToColor()
    val opacity = feature[OPACITY].convertToNumber()
    val width = inMeters(feature[WIDTH].asNumber())
    val lineOffset = inMeters(feature[OFFSET].asNumber())
    val bridgeId = if (bridge) "-bridge" else ""

    LineLayer(
        id = "overlay-lines$bridgeId-side",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = all(commonFilter, !feature.has(DASHED)),
        color = color,
        opacity = opacity,
        width = width,
        offset = lineOffset,
        cap = const(LineCap.Butt),
        join = const(LineJoin.Round),
    )
    LineLayer(
        id = "overlay-lines-dashed$bridgeId-side",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = all(commonFilter, feature.has(DASHED)),
        color = color,
        opacity = opacity,
        width = width,
        offset = lineOffset,
        dasharray = const(listOf(1.5f, 1f)),
        cap = const(LineCap.Butt),
        join = const(LineJoin.Round),
    )
}

/** Draws overlay areas, center lines, outlines, and building extrusions below labels. */
@Composable
@MaplibreComposable
fun StyleableOverlayMainLayers(
    source: Source,
    onClickElement: (ElementKey) -> Unit,
) {
    val opacity = feature[OPACITY].convertToNumber()
    val color = feature[COLOR].convertToColor()
    val outlineColor = feature[OUTLINE_COLOR].convertToColor()
    val width = inMeters(feature[WIDTH].asNumber())
    val casingWidth = inMeters(0.5f)
    val solidCenter = all(feature.isLines(), !feature.has(OFFSET), !feature.has(DASHED))
    val dashedCenter = all(feature.isLines(), !feature.has(OFFSET), feature.has(DASHED))
    val clickHandler = rememberOverlayClickHandler(onClickElement)

    LineLayer(
        id = "overlay-lines-casing",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = solidCenter,
        color = outlineColor,
        opacity = opacity,
        gapWidth = width,
        width = casingWidth,
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
    FillLayer(
        id = "overlay-fills",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = feature.isArea(),
        color = color,
        opacity = opacity,
        onClick = clickHandler,
    )
    LineLayer(
        id = "overlay-lines",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = solidCenter,
        color = color,
        opacity = opacity,
        width = width,
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
        onClick = clickHandler,
    )
    LineLayer(
        id = "overlay-lines-dashed",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = dashedCenter,
        color = color,
        opacity = opacity,
        width = width,
        dasharray = const(listOf(1.5f, 1f)),
        cap = const(LineCap.Butt),
        join = const(LineJoin.Round),
        onClick = clickHandler,
    )
    LineLayer(
        id = "overlay-fills-outline",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = feature.isArea(),
        color = outlineColor,
        opacity = opacity,
        width = casingWidth,
        cap = const(LineCap.Butt),
    )
    FillExtrusionLayer(
        id = "overlay-heights",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = all(feature.isArea(), feature.has(HEIGHT)),
        color = color,
        // Fill-extrusion opacity does not support data expressions in the style specification.
        opacity = const(1f),
        height = feature[HEIGHT].convertToNumber(),
        base = feature[MIN_HEIGHT].convertToNumber(),
    )
}

/** Draws overlay icons and labels above base-map labels. */
@Composable
@MaplibreComposable
internal fun StyleableOverlayLabelLayer(
    source: Source,
    onClickElement: (ElementKey) -> Unit,
) {
    val night = isSystemInDarkTheme()
    val foreground = if (night) Color(0xffccccff) else Color(0xff112244)
    val halo = if (night) Color(0xff2e2e48) else Color.White
    SymbolLayer(
        id = "overlay-symbols",
        source = source,
        minZoom = 17f,
        filter = feature.isPoint(),
        zOrder = const(SymbolZOrder.Source),
        iconImage = image(feature[ICON].convertToString()),
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconColor = const(foreground),
        iconHaloColor = const(halo),
        iconHaloWidth = const(2.5.dp),
        iconAllowOverlap = const(true),
        textField = feature[LABEL].convertToString(),
        textColor = const(foreground),
        textHaloColor = const(halo),
        textHaloWidth = const(2.5.dp),
        textFont = const(listOf("Roboto Regular")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = switch(
            condition(feature.has(ICON), offset(0.em, 1.em)),
            fallback = offset(0.em, 0.em),
        ),
        textSize = const(16.sp),
        textOptional = const(true),
        textAllowOverlap = step(zoom(), fallback = const(false), 21 to const(true)),
        onClick = rememberOverlayClickHandler(onClickElement),
    )
}

// TODO(maplibre-compose): Make layer click hit radius configurable. The common callback currently
// queries only the exact tap coordinate, while Android used a finger-radius rendered-feature box.
@Composable
@MaplibreComposable
private fun rememberOverlayClickHandler(
    onClickElement: (ElementKey) -> Unit,
): (List<Feature<Geometry, JsonObject?>>) -> ClickResult {
    val currentOnClickElement = rememberUpdatedState(onClickElement)
    return remember {
        { features ->
            val properties = features.firstOrNull()?.properties
            val key = properties?.toOverlayElementKey()
            if (key == null || properties.isOverlayElementDisabled()) {
                ClickResult.Pass
            } else {
                currentOnClickElement.value(key)
                ClickResult.Consume
            }
        }
    }
}
