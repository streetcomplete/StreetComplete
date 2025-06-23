package de.westnordost.streetcomplete.screens.main.map2.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
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
import dev.sargunv.maplibrecompose.expressions.dsl.convertToColor
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
    LineLayer(
        id = "overlay-lines-casing",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(
            Feature.isLines(),
            !Feature.has("offset"),
        ),
        opacity = Feature.get("opacity").asNumber(),
        color = Feature.get("outline-color").convertToColor(),
        width = inMeters(0.5f),
        gapWidth = inMeters(Feature.get("width").asNumber()),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
    FillLayer(
        id = "overlay-fills",
        source = source,
        minZoom = MIN_ZOOM,
        filter = Feature.isArea(),
        opacity = Feature.get("opacity").asNumber(),
        color = Feature.get("color").convertToColor(),
        onClick = onClick,
    )
    LineLayer(
        id = "overlay-lines",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(
            Feature.isLines(),
            !Feature.has("offset"),
        ),
        opacity = Feature.get("opacity").asNumber(),
        color = Feature.get("color").convertToColor(),
        width = inMeters(Feature.get("width").asNumber()),
        cap = switch(
            condition(Feature.has("dashed"), const(LineCap.Butt)),
            fallback = const(LineCap.Round)
        ),
        join = const(LineJoin.Round),
        dasharray = switch(
            condition(Feature.has("dashed"), const(listOf(1.5f, 1f))),
            fallback = nil()
        ),
        onClick = onClick,
    )
    LineLayer(
        id = "overlay-fills-outline",
        source = source,
        minZoom = MIN_ZOOM,
        filter = Feature.isArea(),
        opacity = Feature.get("opacity").asNumber(),
        color = Feature.get("outline-color").convertToColor(),
        width = inMeters(0.5f),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round),
    )
    FillExtrusionLayer(
        id = "overlay-heights",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(Feature.isArea(), Feature.has("height")),
        opacity = Feature.get("opacity").asNumber(),
        color = Feature.get("color").convertToColor(),
        height = Feature.get("height").asNumber(),
        base = Feature.get("min-height").asNumber()
    )
}

/** Display styled left-right-of-line map data */
@MaplibreComposable @Composable
fun StyleableOverlaySideLayer(source: Source, isBridge: Boolean) {
    val bridge = Feature.has("bridge")

    LineLayer(
        id = "overlay-lines-side",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(
            Feature.isLines(),
            Feature.has("offset"),
            if (isBridge) bridge else !bridge
        ),
        color = Feature.get("color").convertToColor(),
        width = inMeters(Feature.get("width").asNumber()),
        opacity = Feature.get("opacity").asNumber(),
        offset = inMeters(Feature.get("offset").asNumber()),
        dasharray = switch(
            condition(Feature.has("dashed"), const(listOf(1.5f, 1f))),
            fallback = nil()
        ),
        cap = switch(
            condition(Feature.has("dashed"), const(LineCap.Butt)),
            fallback = const(LineCap.Round)
        ),
        join = const(LineJoin.Round),
    )
}

private val MIN_ZOOM = 14f
