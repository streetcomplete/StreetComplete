package de.westnordost.streetcomplete.screens.main.controls

import android.os.Build
import android.os.Build.VERSION_CODES
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.toPath
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.ktx.proportionalAbsoluteOffset
import de.westnordost.streetcomplete.ui.ktx.proportionalPadding
import de.westnordost.streetcomplete.ui.util.svgPath
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A view for the pointer pin that ought to be displayed at the edge of the screen. The upper left
 *  corner is always the the position at which it is pointing to, i.e. it will be drawn outside of
 *  its bounds when pointing to the right.
 *  [rotate] rotates the pin. As opposed to normal rotation, the content always stays upright */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PointerPinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.surface,
    ),
    contentPadding: Dp = 12.dp,
    rotate: Float = 0f,
    content: @Composable (BoxScope.() -> Unit),
) {
    // workaround for OpenGL issue on Android 6 (see #6001)
    val elevation = if (Build.VERSION.SDK_INT == VERSION_CODES.M) 0.dp else 4.dp
    val pointerPinShape = remember(rotate) { PointerPinShape(rotate) }
    val a = rotate * PI / 180f
    Surface(
        onClick = onClick,
        modifier = modifier
            .proportionalAbsoluteOffset(
                x = (-sin(a) / 2.0 - 0.5).toFloat(),
                y = (cos(a) / 2.0 - 0.5).toFloat(),
            ),
        enabled = enabled,
        shape = pointerPinShape,
        color = colors.backgroundColor(enabled).value,
        contentColor = colors.contentColor(enabled).value,
        elevation = elevation
    ) {
        Box(Modifier
            .proportionalPadding(pointySize)
            .padding(contentPadding)
        ) { content() }
    }
}

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
            x = size.width / pathSize,
            y = size.height / pathSize
        )
        val p = path.toPath()
        p.transform(m)
        return Outline.Generic(p)
    }
}

private const val pathSize = 76f
private val path = svgPath("M 38,62 C 24.745,62 14,51.255 14,38 14.003,32.6405 15.7995,27.4365 19.1035,23.217 L 38,0 56.914,23.2715 C 60.2005,27.4785 61.99,32.6615 62,38 62,51.255 51.255,62 38,62 Z")
private const val pointySize = 14f / 76f

@Preview
@Composable
private fun PreviewPointerPinButton() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        0f, 360f,
        infiniteRepeatable(tween(12000, 0, LinearEasing)),
    )
    PointerPinButton(onClick = {}, rotate = rotation) {
        Image(painterResource(R.drawable.location_dot_small), null)
    }
}
