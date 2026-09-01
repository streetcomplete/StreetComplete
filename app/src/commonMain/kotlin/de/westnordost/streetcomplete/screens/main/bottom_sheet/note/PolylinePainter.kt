package de.westnordost.streetcomplete.screens.main.bottom_sheet.note

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.osm.mapdata.LatLon
import de.westnordost.streetcomplete.data.osmtracks.Trackpoint
import de.westnordost.streetcomplete.ui.ktx.toPx
import de.westnordost.streetcomplete.ui.theme.surfaceContainer
import de.westnordost.streetcomplete.util.math.enclosingBoundingBox
import de.westnordost.streetcomplete.util.math.normalizeLongitude
import kotlin.math.min

class PolylinePainter(
    positions: List<LatLon>,
    private val strokeColor: Color = Color.Black,
    private val strokeWidth: Float = 1f,
    private val backgroundColor: Color = Color.White
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

        drawRect(backgroundColor)
        drawPath(
            path = p,
            color = strokeColor,
            style = Stroke(
                width = strokeWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokeWidth, strokeWidth * 1.5f))
            )
        )
    }
}

private data class Coordinate(val x: Double, var y: Double)

@Composable
fun rememberTrackpointsPainter(
    trackpoints: List<Trackpoint>,
    backgroundColor: Color = MaterialTheme.colors.surfaceContainer,
    strokeColor: Color = MaterialTheme.colors.onSurface,
    strokeWidth: Dp = 1.dp,
): Painter {
    val strokeWidthPx = strokeWidth.toPx()
    return remember(trackpoints, strokeColor, strokeWidthPx) {
        PolylinePainter(
            positions = trackpoints.map { it.position },
            strokeColor = strokeColor,
            strokeWidth = strokeWidthPx,
            backgroundColor = backgroundColor
        )
    }
}
