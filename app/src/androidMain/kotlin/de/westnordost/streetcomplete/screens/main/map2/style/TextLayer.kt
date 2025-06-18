package de.westnordost.streetcomplete.screens.main.map2.style

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.sargunv.maplibrecompose.compose.MaplibreComposable
import dev.sargunv.maplibrecompose.compose.layer.SymbolLayer
import dev.sargunv.maplibrecompose.core.source.Source
import dev.sargunv.maplibrecompose.expressions.ast.Expression
import dev.sargunv.maplibrecompose.expressions.dsl.const
import dev.sargunv.maplibrecompose.expressions.dsl.nil
import dev.sargunv.maplibrecompose.expressions.value.BooleanValue
import dev.sargunv.maplibrecompose.expressions.value.ColorValue
import dev.sargunv.maplibrecompose.expressions.value.FloatValue
import dev.sargunv.maplibrecompose.expressions.value.FormattedValue
import dev.sargunv.maplibrecompose.expressions.value.SymbolPlacement

/** Text layer with default styling */
@Composable @MaplibreComposable
fun TextLayer(
    id: String,
    source: Source,
    sourceLayer: String = "",
    minZoom: Float = 0.0f,
    maxZoom: Float = 24.0f,
    filter: Expression<BooleanValue> = nil(),
    sortKey: Expression<FloatValue> = nil(),
    placement: Expression<SymbolPlacement> = const(SymbolPlacement.Point),
    text: Expression<FormattedValue> = const("").cast(),
    color: Expression<ColorValue> = const(Color.Black),
    haloColor: Expression<ColorValue> = const(Color.Transparent),
) {
    SymbolLayer(
        id = id,
        source = source,
        sourceLayer = sourceLayer,
        minZoom = minZoom,
        maxZoom = maxZoom,
        filter = filter,
        sortKey = sortKey,
        placement = placement,
        textField = text,
        textColor = color,
        textHaloColor = haloColor,
        textHaloWidth = const(2.5.dp),
        textFont = const(listOf("Roboto Regular")),
        textSize = byZoom(1.0 to 13.sp, 24 to 64.sp),
        textPadding = const(12.dp),
    )
}
