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
import org.maplibre.compose.expressions.dsl.all
import org.maplibre.compose.expressions.dsl.asNumber
import org.maplibre.compose.expressions.dsl.condition
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.convertToBoolean
import org.maplibre.compose.expressions.dsl.convertToColor
import org.maplibre.compose.expressions.dsl.convertToNumber
import org.maplibre.compose.expressions.dsl.convertToString
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.dsl.nil
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
import org.maplibre.compose.sources.Source
import org.maplibre.compose.util.FeaturesClickHandler
import org.maplibre.compose.util.MaplibreComposable

/** Display styled map data labels */
@MaplibreComposable
@Composable
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
        filter = feature.isPoint(),
        zOrder = const(SymbolZOrder.Source),
        iconImage = image(feature["icon"]), // TODO
        iconSize = byZoom(17 to 0.5f, 19 to 1f),
        iconColor = const(color),
        iconHaloColor = const(haloColor),
        iconHaloWidth = const(2.5.dp),
        iconAllowOverlap = const(true),
        textField = feature["label"].convertToString(),
        textColor = const(color),
        textHaloColor = const(haloColor),
        textHaloWidth = const(2.5.dp),
        textFont = const(listOf("Roboto Regular")),
        textAnchor = const(SymbolAnchor.Top),
        textOffset = switch(
            condition(feature.has("icon"), offset(0.em, 1.em)),
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
    val dashed = feature["dashed"].convertToBoolean()
    val opacity = feature["opacity"].convertToNumber()
    val color = feature["color"].convertToColor()
    val outlineColor = feature["outline-color"].convertToColor()
    val width = inMeters(feature["width"].asNumber())
    val casingWidth = inMeters(0.5f)

    LineLayer(
        id = "overlay-lines-casing",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(feature.isLines(), !feature.has("offset"), !dashed),
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
        filter = feature.isArea(),
        opacity = opacity,
        color = color,
        onClick = onClick,
    )
    LineLayer(
        id = "overlay-lines",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(feature.isLines(), !feature.has("offset")),
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
        filter = feature.isArea(),
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
        filter = all(feature.isArea(), feature.has("height")),
        // data-driven-styling not supported (see https://maplibre.org/maplibre-style-spec/layers/#fill-extrusion-opacity)
        opacity = const(1f), // cannot use `opacity = opacity`
        color = color,
        height = feature["height"].convertToNumber(),
        base = feature["min-height"].convertToNumber()
    )
}

/** Display styled left-right-of-line map data */
@MaplibreComposable @Composable
fun StyleableOverlaySideLayer(source: Source, isBridge: Boolean) {
    val bridge = feature["bridge"].convertToBoolean()
    val dashed = feature["dashed"].convertToBoolean()
    val opacity = feature["opacity"].convertToNumber()
    val color = feature["color"].convertToColor()
    val width = inMeters(feature["width"].asNumber())
    val offset = inMeters(feature["offset"].asNumber())

    LineLayer(
        id = "overlay-lines-side",
        source = source,
        minZoom = MIN_ZOOM,
        filter = all(
            feature.isLines(),
            feature.has("offset"),
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
