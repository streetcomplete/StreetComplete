package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.screens.main.map2.byZoom
import de.westnordost.streetcomplete.screens.main.map2.inMeters
import de.westnordost.streetcomplete.screens.main.map2.isArea
import de.westnordost.streetcomplete.screens.main.map2.isLines
import de.westnordost.streetcomplete.screens.main.map2.isPoint
import dev.sargunv.maplibrecompose.compose.FeaturesClickHandler
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.FillExtrusionLayer
import dev.sargunv.maplibrecompose.compose.layer.FillLayer
import dev.sargunv.maplibrecompose.compose.layer.LineLayer
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.core.source.Source
import dev.sargunv.maplibrecompose.expressions.dsl.Feature
import dev.sargunv.maplibrecompose.expressions.dsl.all
import dev.sargunv.maplibrecompose.expressions.dsl.asNumber
import dev.sargunv.maplibrecompose.expressions.dsl.condition
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.convertToBoolean
import dev.sargunv.maplibrecompose.expressions.dsl.convertToColor
import dev.sargunv.maplibrecompose.expressions.dsl.convertToNumber
import dev.sargunv.maplibrecompose.expressions.dsl.convertToString
import dev.sargunv.maplibrecompose.expressions.dsl.nil
import dev.sargunv.maplibrecompose.expressions.dsl.offset
import dev.sargunv.maplibrecompose.expressions.dsl.not
import dev.sargunv.maplibrecompose.expressions.dsl.step
import dev.sargunv.maplibrecompose.expressions.dsl.switch
import dev.sargunv.maplibrecompose.expressions.dsl.zoom
import dev.sargunv.maplibrecompose.expressions.value.LineCap
import dev.sargunv.maplibrecompose.expressions.value.LineJoin
import dev.sargunv.maplibrecompose.expressions.value.SymbolAnchor
import dev.sargunv.maplibrecompose.expressions.value.SymbolZOrder

/** Display styled map data labels */
@MaplibreComposable @Composable
fun StyleableOverlayLabelLayer(
    source: Source,
    color: Color,
    haloColor: Color,
    onClick: FeaturesClickHandler? = null,
) {
    SymbolLayer(
        id = "overlay-symbols",
        source = source,
        minZoom = 17f,
        filter = Feature.isPoint(),
        zOrder = const(SymbolZOrder.Source),
        iconImage = Feature.get("icon"), // TODO
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconColor = const(color),
        iconHaloColor = const(haloColor),
        iconHaloWidth = const(2.5.dp),
        iconAllowOverlap = const(true),
        textField = Feature.get("label").convertToString(),
        textColor = const(color),
        textHaloColor = const(haloColor),
        textHaloWidth = const(2.5.dp),
        textFont = const(listOf("Roboto Regular")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = switch(
            condition(Feature.has("icon"), offset(0.em, 1.em)),
            fallback = offset(0.em, 0.em)
        ),
        textSize = const(16.sp),
        textOptional = const(true),
        textAllowOverlap = step(
            input = zoom(),
            fallback = const(false),
            21 to const(true)
        ),
        onClick = onClick
    )
}

/** Display styled map data */
@MaplibreComposable @Composable
fun StyleableOverlayLayers(
    source: Source,
    onClick: FeaturesClickHandler? = null,
) {
    val dashed = Feature.get("dashed").convertToBoolean()
    val opacity = Feature.get("opacity").convertToNumber()
    val color = Feature.get("color").convertToColor()
    val outlineColor = Feature.get("outline-color").convertToColor()
    val width = inMeters(Feature.get("width").asNumber())
    val casingWidth = inMeters(0.5f)

    LineLayer(
        id = "overlay-lines-casing",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(Feature.isLines(), !Feature.has("offset"), !dashed),
        opacity = opacity,
        color = outlineColor,
        width = casingWidth,
        gapWidth = width,
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
    FillLayer(
        id = "overlay-fills",
        source = source,
        minZoom = MIN_ZOOM,
        filter = Feature.isArea(),
        opacity = opacity,
        color = color,
        onClick = onClick,
    )
    LineLayer(
        id = "overlay-lines",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(Feature.isLines(), !Feature.has("offset")),
        opacity = opacity,
        color = color,
        width = width,
        dasharray = switch(
            condition(dashed, const(listOf(1.5f, 1f))),
            fallback = nil()
        ),
        cap = switch(
            condition(dashed, const(LineCap.Butt)),
            fallback = const(LineCap.Round)
        ),
        join = const(LineJoin.Round),
        onClick = onClick,
    )
    LineLayer(
        id = "overlay-fills-outline",
        source = source,
        minZoom = MIN_ZOOM,
        filter = Feature.isArea(),
        opacity = opacity,
        color = outlineColor,
        width = casingWidth,
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
    FillExtrusionLayer(
        id = "overlay-heights",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(Feature.isArea(), Feature.has("height")),
        // data-driven-styling not supported (see https://maplibre.org/maplibre-style-spec/layers/#fill-extrusion-opacity)
        opacity = const(1f), // cannot use `opacity = opacity`
        color = color,
        height = Feature.get("height").convertToNumber(),
        base = Feature.get("min-height").convertToNumber()
    )
}

/** Display styled left-right-of-line map data */
@MaplibreComposable @Composable
fun StyleableOverlaySideLayer(source: Source, isBridge: Boolean) {
    val bridge = Feature.get("bridge").convertToBoolean()
    val dashed = Feature.get("dashed").convertToBoolean()
    val opacity = Feature.get("opacity").convertToNumber()
    val color = Feature.get("color").convertToColor()
    val width = inMeters(Feature.get("width").asNumber())
    val offset = inMeters(Feature.get("offset").asNumber())

    LineLayer(
        id = "overlay-lines-side",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(
            Feature.isLines(),
            Feature.has("offset"),
            if (isBridge) bridge else !bridge
        ),
        color = color,
        width = width,
        opacity = opacity,
        offset = offset,
        dasharray = switch(
            condition(dashed, const(listOf(1.5f, 1f))),
            fallback = nil()
        ),
        cap = switch(
            condition(dashed, const(LineCap.Butt)),
            fallback = const(LineCap.Round)
        ),
        join = const(LineJoin.Round),
    )
}

private val MIN_ZOOM = 14f
