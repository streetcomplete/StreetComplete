package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.toOffset
import kotlin.math.max
import kotlin.math.min

/** Image that can be zoomed in and panned */
@Composable
fun ZoomableImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    initialZoom: Float = 1f,
    zoomRange: ClosedFloatingPointRange<Float> = 1f..3f,
) {
    var scale by remember { mutableFloatStateOf(initialZoom.coerceIn(zoomRange)) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, rotation ->
                    val center = size.center.toOffset()
                    val newScale = (scale * zoom).coerceIn(zoomRange)
                    val newZoom = newScale / scale
                    val newOffset = (offset + pan) * newZoom + ((centroid - center) * (1f - newZoom))
                    scale = newScale
                    val minX = center.x * (newScale - 1f)
                    val minY = center.y * (newScale - 1f)
                    offset = Offset(
                        newOffset.x.coerceIn(min(-minX, minX), max(-minX, minX)),
                        newOffset.y.coerceIn(min(-minY, minY), max(-minY, minY))
                    )
                }
            }
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationX = offset.x
                translationY = offset.y
            },
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
    )
}
