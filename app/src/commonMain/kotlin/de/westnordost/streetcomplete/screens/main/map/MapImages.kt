package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pin
import de.westnordost.streetcomplete.resources.pin_shadow
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.util.sdf.convertToSdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.style.StyleHandleException
import kotlin.math.ceil
import kotlin.math.min

@Composable
fun rememberMapImages(mapState: MapState): MapImages {
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val pin = painterResource(Res.drawable.pin)
    val pinShadow = painterResource(Res.drawable.pin_shadow)
    return remember(mapState, density, layoutDirection, pin, pinShadow) {
        MapImages(mapState, density, layoutDirection, pin, pinShadow)
    }
}

/** Adds the images that the map layers refer to by name to the style, each once. */
@Stable
class MapImages internal constructor(
    private val map: MapState,
    private val density: Density,
    private val layoutDirection: LayoutDirection,
    private val pin: Painter,
    private val pinShadow: Painter,
) {
    private val mutex = Mutex()

    /** Adds quest pins with the given icons, named "pin_" + icon id */
    suspend fun addPins(icons: Map<DrawableResource, Painter>) = add(icons) { icon, painter ->
        Image("pin_" + icon.id, PinPainter(painter, pin, pinShadow).toImageBitmap(PIN_SIZE))
    }

    /** Adds the given icons, named by their icon id. Monochrome preset icons are added as SDF, so
     *  that layers can color and outline them. */
    suspend fun addIcons(icons: Map<DrawableResource, Painter>) = add(icons) { icon, painter ->
        val id = icon.id ?: return@add null
        val sdf = id.startsWith("preset_")
        val bitmap = painter.toImageBitmap(MAX_ICON_SIZE)
        Image(id, if (sdf) bitmap.toSdf(with(density) { SDF_RADIUS.toPx() }.toDouble()) else bitmap, sdf)
    }

    private suspend fun add(
        icons: Map<DrawableResource, Painter>,
        create: (DrawableResource, Painter) -> Image?,
    ) {
        snapshotFlow { map.style.loadState }.first { it == StyleLoadState.Ready }
        mutex.withLock {
            for ((icon, painter) in icons) {
                val image = withContext(Dispatchers.Default) { create(icon, painter) } ?: continue
                if (map.style.images[image.id] != null) continue
                try {
                    map.style.images.set(image.id, image.bitmap, sdf = image.sdf)
                } catch (e: StyleHandleException) {
                    // the style was replaced meanwhile; the next call adds the image again
                    return
                }
            }
        }
    }

    private class Image(val id: String, val bitmap: ImageBitmap, val sdf: Boolean = false)

    /** Draws the painter into an image of its intrinsic size, at most [maxSize] wide and high */
    private fun Painter.toImageBitmap(maxSize: Dp): ImageBitmap {
        val maxPx = with(density) { maxSize.toPx() }
        val size = Size(
            min(intrinsicSize.width.takeIf { it.isFinite() } ?: maxPx, maxPx),
            min(intrinsicSize.height.takeIf { it.isFinite() } ?: maxPx, maxPx),
        )
        val bitmap = ImageBitmap(ceil(size.width).toInt(), ceil(size.height).toInt())
        CanvasDrawScope().draw(density, layoutDirection, Canvas(bitmap), size) {
            with(this@toImageBitmap) { draw(size) }
        }
        return bitmap
    }

    private companion object {
        val PIN_SIZE = 71.dp
        val MAX_ICON_SIZE = 48.dp
        val SDF_RADIUS = 8.dp
    }
}

/** Converts this image to a signed distance field, adding a border of [radius] around it */
private fun ImageBitmap.toSdf(radius: Double, cutoff: Double = 0.25): ImageBitmap {
    val buffer = ceil(radius * (1.0 - cutoff)).toInt()
    val w = width + 2 * buffer
    val h = height + 2 * buffer
    val pixels = IntArray(w * h)
    readPixels(pixels, 0, 0, width, height, w * buffer + buffer, w)
    convertToSdf(pixels, w, radius, cutoff)
    return pixels.toImageBitmap(w, h)
}

/** Creates an image from unpremultiplied ARGB pixels */
internal expect fun IntArray.toImageBitmap(width: Int, height: Int): ImageBitmap
