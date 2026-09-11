package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter

/** Painter that paints the given [painter] with the given [colorFilter] and optionally a custom
 *  [alpha]. */
class ColorFilterPainter(
    private val painter: Painter,
    private val colorFilter: ColorFilter,
    private val alpha: Float = 1.0f,
) : Painter() {

    override val intrinsicSize = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(painter) {
            draw(size, alpha = alpha, colorFilter = colorFilter)
        }
    }
}
