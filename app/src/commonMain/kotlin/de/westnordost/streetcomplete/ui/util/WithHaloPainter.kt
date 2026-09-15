package de.westnordost.streetcomplete.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Painter that draws an outline (a.k.a. halo) of given [outlineWidth] and [outlineColor] around
 *  opaque parts of the given [painter] */
class WithHaloPainter(
    val painter: Painter,
    val density: Density,
    val outlineWidth: Dp = 1.dp,
    val outlineColor: Color = Color.White,
) : Painter() {
    override val intrinsicSize: Size
        get() {
            val padding = outlineWidth.value * density.density
            return Size(
                width = painter.intrinsicSize.width + padding * 2,
                height = painter.intrinsicSize.height + padding * 2
            )
        }

    override fun DrawScope.onDraw() {
        val padding = outlineWidth.value * density
        val alphaImage = createAlphaImageBitmap(size, Density(density), layoutDirection) {
            inset(padding) {
                with(painter) { draw(size) }
            }
        }
        val shader = createDilateShader(alphaImage, padding, outlineColor)
        drawRect(ShaderBrush(shader))

        inset(padding) {
            with(painter) { draw(size) }
        }
    }
}

/** Create a bitmap containing only the alpha channel from whatever is drawn in the [block] */
private fun createAlphaImageBitmap(
    size: Size,
    density: Density,
    layoutDirection: LayoutDirection,
    block: DrawScope.() -> Unit
): ImageBitmap {
    val imageBitmap = ImageBitmap(
        width = size.width.toInt(),
        height = size.height.toInt(),
        config = ImageBitmapConfig.Alpha8
    )
    val canvas = Canvas(imageBitmap)
    val drawScope = CanvasDrawScope()
    drawScope.draw(density, layoutDirection, canvas, size, block)
    return imageBitmap
}
