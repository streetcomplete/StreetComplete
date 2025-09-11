package de.westnordost.streetcomplete.ui.util

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.painter.Painter

class CircularClippedPainter(private val painter: Painter) : Painter() {
    override val intrinsicSize: Size get() = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        val path = Path().apply { addOval(Rect(Offset.Zero, size)) }
        clipPath(path) { with (painter) { draw(size) } }
    }
}
