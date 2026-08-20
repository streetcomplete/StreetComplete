package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.graphics.painter.Painter

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
