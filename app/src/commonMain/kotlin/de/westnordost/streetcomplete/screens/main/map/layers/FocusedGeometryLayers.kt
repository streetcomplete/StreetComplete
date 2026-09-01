package de.westnordost.streetcomplete.screens.main.map.layers

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.screens.main.map.isArea
import de.westnordost.streetcomplete.screens.main.map.isPoint
import de.westnordost.streetcomplete.screens.main.map.toGeometry
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.value.LineCap
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.FillLayer
import org.maplibre.compose.layers.LineLayer
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.util.MaplibreComposable
import kotlin.math.PI
import kotlin.math.sin

private const val HIGHLIGHT_CYCLE_MILLIS = 1200
private val HighlightColor = Color(0xffd14000)

/** Displays the pulsing point, line, or polygon geometry of the currently focused element. */
@Composable
@MaplibreComposable
fun FocusedGeometryLayers(geometry: ElementGeometry) {
    val transition = rememberInfiniteTransition(label = "FocusedGeometry")
    val cycleFraction by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(HIGHLIGHT_CYCLE_MILLIS, easing = LinearEasing)
        ),
        label = "FocusedGeometryBreathing",
    )
    val style = focusedGeometryStyle(cycleFraction)

    // TODO(maplibre-compose): Restore the legacy source's volatile flag when GeoJsonOptions exposes it.
    val source = rememberGeoJsonSource(
        data = GeoJsonData.Features(geometry.toGeometry())
    )

    FillLayer(
        id = "focus-geo-fill",
        source = source,
        filter = feature.isArea(),
        opacity = const(0.3f),
        color = const(HighlightColor),
    )
    LineLayer(
        id = "focus-geo-lines",
        source = source,
        opacity = const(style.opacity),
        color = const(HighlightColor),
        width = const(style.lineWidth.dp),
        cap = const(LineCap.Round),
    )
    CircleLayer(
        id = "focus-geo-circle",
        source = source,
        filter = feature.isPoint(),
        opacity = const(style.opacity),
        color = const(HighlightColor),
        radius = const(style.circleRadius.dp),
    )
}

internal data class FocusedGeometryStyle(
    val opacity: Float,
    val lineWidth: Float,
    val circleRadius: Float,
)

internal fun focusedGeometryStyle(cycleFraction: Float): FocusedGeometryStyle {
    val breathing = sin(cycleFraction * 2f * PI.toFloat()) / 2f + 0.5f
    val sizeFactor = breathing + 0.75f
    return FocusedGeometryStyle(
        opacity = (1f - breathing) * 0.5f + 0.15f,
        lineWidth = 10f * sizeFactor,
        circleRadius = 12f * sizeFactor,
    )
}
