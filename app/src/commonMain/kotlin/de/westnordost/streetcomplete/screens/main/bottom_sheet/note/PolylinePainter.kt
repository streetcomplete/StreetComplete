package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import kotlin.math.min

class PolylinePainter(
    positions: List<LatLon>,
    private val strokeColor: Color = Color.Black,
    private val strokeWidth: Float = 1f,
) : Painter() {
    private val coordinates: List<Coordinate>
    private val width: Double
    private val height: Double

    override val intrinsicSize: Size = Size(Float.NaN, Float.NaN)

    init {
        val bbox = positions.enclosingBoundingBox()
        // we change the origin to the upper left corner
        coordinates = positions.map {
            Coordinate(
                // should handle 180th meridian correctly
                x = normalizeLongitude(it.longitude - bbox.min.longitude),
                // LatLon coordinate system is upside down to graphics coordinate system
                y = -(it.latitude - bbox.max.latitude),
            )
        }
        width = coordinates.maxOf { it.x }
        height = coordinates.maxOf { it.y }
    }

    override fun DrawScope.onDraw() {
        val scaleX = size.width / width
        val scaleY = size.height / height
        val scale = min(scaleX, scaleY)
        val aspectRatio = scaleX / scaleY
        val offset = Offset(
            x = ((size.width - width * scale) / 2.0).toFloat(),
            y = ((size.height - height * scale) / 2.0).toFloat()
        )
        val p = Path()

        val first = coordinates.first()
        p.moveTo(offset.x + (first.x * scale).toFloat(), offset.y + (first.y * scale).toFloat())
        for (c in coordinates) {
            p.lineTo(offset.x + (c.x * scale).toFloat(), offset.y + (c.y * scale).toFloat())
        }

        drawPath(
            path = p,
            color = strokeColor,
            style = Stroke(width = strokeWidth)
        )
    }
}

private data class Coordinate(val x: Double, var y: Double)
