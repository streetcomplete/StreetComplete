package de.westnordost.streetcomplete.screens.main.controls

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.layout.layout
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.theme.divider
import de.westnordost.streetcomplete.util.DistanceFormatter
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A view for the pointer pin displayed at the edge of the screen.
 *
 *  Layout geometry (Horizontal base pin, pointing Top/Tip at (38,0) rotated -90° to point left):
 *  ```
 *   .---------.
 *  < (*) 120 m )
 *   `---------'
 *  ```
 *  [rotate] rotates the outer pin capsule around its tip anchor.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PointerPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
    rotate: Float = 0f,
    distanceInMeters: Double? = null,
    icon: Painter = painterResource(Res.drawable.location_dot_small),
) {
    val distanceText = distanceInMeters?.takeIf { it > 0.0 }?.let { DistanceFormatter.format(it) }
    val pointerPinShape = remember { PointerPinShape() }
    val a = (rotate * PI / 180f).toFloat()

    // Base horizontal shape tip is at (0, h/2) pointing LEFT.
    // Rotating by (rotate + 90°) points tip UP at rotate=0° and rotates clockwise with map angle.
    val outerRotation = rotate + 90f

    Surface(
        onClick = onClick,
        modifier = modifier
            // Shifts the button so its pointy tip lands at the position set by
            // the caller (e.g. via absoluteOffset), rather than the bounding-box corner
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                val w = placeable.width.toFloat()
                val h = placeable.height.toFloat()
                val xFactor = -0.5f - 0.5f * sin(a)
                val yFactor = -0.5f + (w / (2f * h)) * cos(a)
                layout(placeable.width, placeable.height) {
                    placeable.place((xFactor * w).toInt(), (yFactor * h).toInt())
                }
            }
            .graphicsLayer { rotationZ = outerRotation },
        enabled = enabled,
        shape = pointerPinShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        border = BorderStroke(1.dp, MaterialTheme.colors.divider),
        elevation = 4.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 16.dp, end = 22.dp, top = 16.dp, bottom = 16.dp)
        ) {
            Image(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            if (distanceText != null) {
                // Text aligns with lozenge capsule and flips 180° when needed so it is never upside down
                var normRotation = outerRotation % 360f
                if (normRotation > 180f) normRotation -= 360f
                if (normRotation < -180f) normRotation += 360f
                val textRotation = if (normRotation > 90f || normRotation < -90f) 180f else 0f

                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = distanceText,
                    style = MaterialTheme.typography.caption.copy(fontSize = 12.sp),
                    color = MaterialTheme.colors.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.graphicsLayer { rotationZ = textRotation }
                )
            }
        }
    }
}

private class PointerPinShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val w = size.width
        val h = size.height
        // Path base width is 76f. dw horizontally elongates the capsule along its length.
        val baseW = 76f
        val dw = ((w / h) * 76f - baseW).coerceAtLeast(0f)

        // Pointy tip is at (0,38). Capsule extends horizontally to the right.
        val pathString = "M 0,38 L 23.217,19.1035 C 27.4365,15.7995 32.6405,14.003 38,14 L ${38f + dw},14 C ${51.255f + dw},14 ${62f + dw},24.745 ${62f + dw},38 C ${62f + dw},51.255 ${51.255f + dw},62 ${38f + dw},62 L 38,62 C 32.6405,61.99 27.4365,60.2005 23.217,56.914 Z"
        val p = PathParser().parsePathString(pathString).toNodes().toPath()
        val m = Matrix()
        m.scale(
            x = w / (baseW + dw),
            y = h / 76f
        )
        p.transform(m)
        return Outline.Generic(p)
    }
}

@Preview(widthDp = 240, heightDp = 240, locale = "de")
@Composable
private fun PreviewPointerPinButton() {
    val infiniteTransition = rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val distance by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart
    ) {
        Box(modifier = Modifier.offset(x = 120.dp, y = 120.dp)) {
            PointerPinButton(
                onClick = {},
                rotate = rotation,
                distanceInMeters = distance.toDouble()
            )
        }
    }
}
