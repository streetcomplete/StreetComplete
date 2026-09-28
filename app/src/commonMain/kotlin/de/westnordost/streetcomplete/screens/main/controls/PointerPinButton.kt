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
        modifier = modifier.placedTowards(targetPosition, placement),
        enabled = enabled,
        shape = PointerPinShape(placement.angleDegrees),
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.divider),
        elevation = 4.dp
    ) {
        Box(Modifier
            .proportionalPadding(14f / 76f)
            .padding(contentPadding)
        ) { content() }
    }
}

// we need to pass and let the shape itself rotate itself rather than just rotating the parent
// surface composable to work around a bug in Android 7.1.1 (API level 25): The rotation
// (`Modifier.rotate`) of a composable is ignored for clipping (`Modifier.clip`). `Surface` always
// clips to its given `shape`. (see #7176)
private class PointerPinShape(val rotation: Float = 0f) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val m = Matrix()
        val halfWidth = size.width / 2
        val halfHeight = size.height / 2
        m.translate(halfWidth, halfHeight)
        m.rotateZ(rotation)
        m.translate(-halfWidth, -halfHeight)
        m.scale(
            x = size.width / PATH_SIZE,
            y = size.height / PATH_SIZE
        )
        val p = PATH.toPath()
        p.transform(m)
        return Outline.Generic(p)
    }

    companion object {
        private val PATH_SIZE = 76f
        private val PATH by lazy { PathParser()
            .parsePathString("M 38,62 C 24.745,62 14,51.255 14,38 14.003,32.6405 15.7995,27.4365 19.1035,23.217 L 38,0 56.914,23.2715 C 60.2005,27.4785 61.99,32.6615 62,38 62,51.255 51.255,62 38,62 Z")
            .toNodes()
        }
    }
}
