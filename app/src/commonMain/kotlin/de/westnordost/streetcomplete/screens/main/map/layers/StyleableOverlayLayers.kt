package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import de.westnordost.streetcomplete.ui.ktx.id
import kotlinx.serialization.json.JsonObject
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.eq
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.nil
import org.maplibre.compose.expressions.dsl.not
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.dsl.step
import org.maplibre.compose.expressions.dsl.switch
import org.maplibre.compose.expressions.dsl.zoom
import org.maplibre.compose.expressions.value.ImageValue
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
import org.maplibre.compose.sources.GeoJsonSourceHandle
import org.maplibre.compose.sources.Source
import org.maplibre.compose.util.ClickResult
import org.maplibre.compose.util.MaplibreComposable
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Geometry

private const val OVERLAY_SOURCE_ID = "overlay-source"
private const val MIN_ZOOM = 14

/** Creates the one source shared across the overlay's four map-style insertion points. */
@Composable
fun rememberStyleableOverlaySource(
    mapState: MapState,
    styledElements: Collection<StyledElement>,
): GeoJsonSource {
    val data = GeoJsonData.Features(
        FeatureCollection(styledElements.flatMap(StyledElement::toOverlayFeatures))
    )
    val options = remember { GeoJsonOptions(minZoom = MIN_ZOOM) }

    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    // Keep the definition stable to avoid hiding Android's render surface during large declarative
    // GeoJSON replacements. A newly published handle retries an update that lost a style race.
    val source = remember(options) { GeoJsonSource(OVERLAY_SOURCE_ID, data, options) }
    val sourceHandle = mapState.style.sources[OVERLAY_SOURCE_ID] as? GeoJsonSourceHandle
    LaunchedEffect(sourceHandle, data) {
        try {
            sourceHandle?.setData(data)
        } catch (error: IllegalStateException) {
            // A replacement generation publishes another handle and restarts this effect.
            if (!error.isStyleHandleRace()) throw error
        }
    }
    return source
}

/** Draws left/right road strokes below ordinary or bridge roads. */
@Composable
@MaplibreComposable
fun StyleableOverlaySideLayers(
    source: Source,
    bridge: Boolean,
    visible: Boolean = true,
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
        visible = visible,
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
        visible = visible,
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
    visible: Boolean = true,
    onClickElement: (ElementKey) -> Unit,
) {
    val opacity = feature[OPACITY].convertToNumber()
    val color = feature[COLOR].convertToColor()
    val outlineColor = feature[OUTLINE_COLOR].convertToColor()
    val width = inMeters(feature[WIDTH].asNumber())
    val casingWidth = inMeters(0.5f)
    val solidCenter = all(feature.isLines(), !feature.has(OFFSET), !feature.has(DASHED))
    val dashedCenter = all(feature.isLines(), !feature.has(OFFSET), feature.has(DASHED))
    val clickHandler = overlayClickHandler(onClickElement)

    LineLayer(
        id = "overlay-lines-casing",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = solidCenter,
        visible = visible,
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
        visible = visible,
        color = color,
        opacity = opacity,
        onClick = clickHandler,
    )
    LineLayer(
        id = "overlay-lines",
        source = source,
        minZoom = MIN_ZOOM.toFloat(),
        filter = solidCenter,
        visible = visible,
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
        visible = visible,
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
        visible = visible,
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
        visible = visible,
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
fun StyleableOverlayLabelLayer(
    source: Source,
    styledElements: Collection<StyledElement>,
    visible: Boolean = true,
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
        visible = visible,
        zOrder = const(SymbolZOrder.Source),
        iconImage = overlayIconExpression(styledElements),
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
        onClick = overlayClickHandler(onClickElement),
    )
}

@Composable
private fun overlayIconExpression(
    styledElements: Collection<StyledElement>,
): Expression<ImageValue> {
    val resources = styledElements.mapNotNull(StyledElement::overlayIcon).distinct()
    val conditions = resources.mapNotNull { resource ->
        val id = resource.id ?: return@mapNotNull null
        condition(
            test = all(feature.has(ICON), feature[ICON].convertToString() eq const(id)),
            output = overlayImage(resource, id),
        )
    }
    return switch(*conditions.toTypedArray(), fallback = nil())
}

@Composable
private fun overlayImage(resource: DrawableResource, id: String): Expression<ImageValue> =
    image(painterResource(resource), drawAsSdf = id.startsWith("preset_"))

// TODO(maplibre-compose): Make layer click hit radius configurable. The common callback currently
// queries only the exact tap coordinate, while Android used a finger-radius rendered-feature box.
private fun overlayClickHandler(
    onClickElement: (ElementKey) -> Unit,
): (List<Feature<Geometry, JsonObject?>>) -> ClickResult = { features ->
    val properties = features.firstOrNull()?.properties
    val key = properties?.toOverlayElementKey()
    if (key == null || properties.isOverlayElementDisabled()) {
        ClickResult.Pass
    } else {
        onClickElement(key)
        ClickResult.Consume
    }
}
