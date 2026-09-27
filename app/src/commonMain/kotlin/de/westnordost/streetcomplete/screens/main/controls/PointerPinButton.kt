package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.proportionalPadding
import de.westnordost.streetcomplete.ui.theme.divider
import org.maplibre.compose.overlay.MapOverlayScope
import org.maplibre.compose.overlay.rememberPlacedTowardsState
import org.maplibre.spatialk.geojson.Position

/** A pointer at the edge of the unobstructed map, shown while [targetPosition] lies outside
 *  its inscribed ellipse. The pin points towards the target; its content stays upright. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapOverlayScope.PointerPinButton(
    targetPosition: Position,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
    contentPadding: Dp = 12.dp,
    content: @Composable (BoxScope.() -> Unit),
) {
    val placement = rememberPlacedTowardsState()
    Surface(
        onClick = onClick,
        modifier = modifier
            .placedTowards(targetPosition, placement)
            // Placement sets the angle during layout; draw-time rotation uses it in the same frame.
            .graphicsLayer { rotationZ = placement.angleDegrees },
        enabled = enabled,
        shape = PointerPinShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.divider),
        elevation = 4.dp
    ) {
        Box(Modifier
            .graphicsLayer { rotationZ = -placement.angleDegrees }
            .proportionalPadding(14f / 76f)
            .padding(contentPadding)
        ) { content() }
    }
}

private object PointerPinShape : Shape {

    private val pathSize = 76f
    private val path = PathParser()
        .parsePathString("M 38,62 C 24.745,62 14,51.255 14,38 14.003,32.6405 15.7995,27.4365 19.1035,23.217 L 38,0 56.914,23.2715 C 60.2005,27.4785 61.99,32.6615 62,38 62,51.255 51.255,62 38,62 Z")
        .toNodes()

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val m = Matrix()
        m.scale(
            x = size.width / pathSize,
            y = size.height / pathSize
        )
        val p = path.toPath()
        p.transform(m)
        return Outline.Generic(p)
    }
}
