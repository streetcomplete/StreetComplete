package de.westnordost.streetcomplete.screens.main.map2.layers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map2.isArea
import de.westnordost.streetcomplete.screens.main.map2.isLines
import de.westnordost.streetcomplete.screens.main.map2.isPoint
import de.westnordost.streetcomplete.screens.main.map2.toGeometry
import org.maplibre.compose.expressions.dsl.any
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.expressions.value.LineJoin
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import kotlin.math.PI
import kotlin.math.cos

/** Display focused element geometry. */
@MaplibreComposable
@Composable
fun FocusedGeometryLayers(geometry: ElementGeometry) {
    // breathing effect for highlight
    val highlightTransition = rememberInfiniteTransition()
    val highlight by highlightTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1200, 0, LinearEasing))
    )
    val breathing = -cos(highlight * 2 * PI) / 2.0 + 0.5 // 0..1
    val opacity = ((1 - breathing) * 0.5 + 0.5).toFloat() // 1 .. 0.5
    val lineWidth = ((breathing + 1) * 8).dp // 8..16
    val circleRadius = ((breathing + 1) * 10).dp // 10..20

    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(geometry.toGeometry())
    )

    FillLayer(
        id = "focus-geo-fill",
        source = source,
        filter = feature.isArea(),
        opacity = const(0.3f),
        color = const(MaterialTheme.colors.secondary),
    )
    LineLayer(
        id = "focus-geo-lines",
        source = source,
        filter = any(feature.isArea(), feature.isLines()),
        opacity = const(opacity),
        color = const(MaterialTheme.colors.secondary),
        width = const(lineWidth),
        cap = const(LineCap.Round),
        join = const(LineJoin.Round)
    )
    CircleLayer(
        id = "focus-geo-circle",
        source = source,
        filter = feature.isPoint(),
        opacity = const(opacity),
        color = const(MaterialTheme.colors.secondary),
        radius = const(circleRadius),
    )
}
