package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.resources.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.util.sdf.convertToSdf
import kotlin.math.ceil
import org.jetbrains.compose.resources.painterResource

/** Draws a (quest) pin */
class PinPainter(
    private val iconPainter: Painter,
    private val pinPainter: Painter,
    private val pinShadowPainter: Painter,
) : Painter() {
    override val intrinsicSize: Size
        get() = pinShadowPainter.intrinsicSize

    override fun DrawScope.onDraw() {
        val sX = size.width / 71f
        val sY = size.height / 71f

        with(pinShadowPainter) { draw(size) }
        inset(left = 14f * sX, top = 5f * sY, right = 5f * sX, bottom = 0f * sY) {
            with(pinPainter) { draw(size) }
            inset(left = 2f * sX, top = 2f * sY, right = 2f * sX, bottom = 16f * sY) {
                with(iconPainter) { draw(size) }
            }
        }
    }
}

@Composable
fun pinPainter(iconPainter: Painter): Painter {
    val pinPainter = painterResource(Res.drawable.pin)
    val pinShadowPainter = painterResource(Res.drawable.pin_shadow)
    return remember(iconPainter, pinPainter, pinShadowPainter) {
        PinPainter(
            iconPainter = iconPainter,
            pinPainter = pinPainter,
            pinShadowPainter = pinShadowPainter
        )
    }
}

/** Rasterizes one pin at the same 71dp size as the legacy Android map. */
internal fun Painter.toPinImageBitmap(
    density: Density,
    layoutDirection: LayoutDirection,
): ImageBitmap = toImageBitmap(
    density = density,
    layoutDirection = layoutDirection,
    size = DpSize(71.dp, 71.dp),
)

/** Rasterizes a painter off the Compose thread using the same sizing rule as MapLibre Compose. */
internal fun Painter.toImageBitmap(
    density: Density,
    layoutDirection: LayoutDirection,
    size: DpSize? = null,
    colorFilter: ColorFilter? = null,
): ImageBitmap {
    val pixelSize = with(density) {
        size?.let { Size(it.width.toPx(), it.height.toPx()) }
            ?: intrinsicSize.takeIf { it.width > 0f && it.height > 0f }
            ?: Size(16.dp.toPx(), 16.dp.toPx())
    }
    val bitmap = ImageBitmap(pixelSize.width.toInt(), pixelSize.height.toInt())
    CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), pixelSize) {
        with(this@toImageBitmap) { draw(pixelSize, colorFilter = colorFilter) }
    }
    return bitmap
}

/** Converts an alpha icon to the signed-distance representation expected by a tinted style image. */
internal fun ImageBitmap.toSdf(
    radius: Double = 8.0,
    cutoff: Double = 0.25,
): ImageBitmap {
    val buffer = ceil(radius * (1.0 - cutoff)).toInt()
    val targetWidth = width + 2 * buffer
    val pixels = IntArray(targetWidth * (height + 2 * buffer))
    readPixels(pixels, bufferOffset = targetWidth * buffer + buffer, stride = targetWidth)
    convertToSdf(pixels, targetWidth, radius, cutoff)
    return pixels.toPlatformImageBitmap(targetWidth, pixels.size / targetWidth)
}
