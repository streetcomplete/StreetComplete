package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.pin
import de.westnordost.streetcomplete.resources.pin_shadow
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.ui.util.ColorFilterPainter
import de.westnordost.streetcomplete.ui.util.WithHaloPainter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.map.MapState
import org.maplibre.compose.map.ResolvedStyleImage
import org.maplibre.compose.map.StyleLoadState
import org.maplibre.compose.style.StyleHandleException
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
    suspend fun addPins(icons: Map<DrawableResource, Painter>) =
        add(icons, { "pin_" + it.id }) { _, painter ->
            val pinPainter = PinPainter(painter, pin, pinShadow)
            ResolvedStyleImage.fromPainter(
                pinPainter, density, layoutDirection, size = pinPainter.sizeAtMost(PIN_SIZE)
            )
        }

    /** Adds the given icons with a halo in [haloColor], named [iconNamePrefix] + icon id.
     *  Monochrome preset icons are drawn in [color]. */
    suspend fun addIcons(icons: Map<DrawableResource, Painter>, color: Color, haloColor: Color) {
        val prefix = iconNamePrefix(color, haloColor)
        add(icons, { icon -> icon.id?.let { prefix + it } }) { icon, painter ->
            val isMonochrome = icon.id?.startsWith("preset_") == true
            val tinted = if (isMonochrome) ColorFilterPainter(painter, ColorFilter.tint(color)) else painter
            val withHalo = WithHaloPainter(tinted, density, ICON_HALO_WIDTH, haloColor)
            ResolvedStyleImage.fromPainter(
                withHalo, density, layoutDirection,
                size = withHalo.sizeAtMost(MAX_ICON_SIZE + ICON_HALO_WIDTH * 2),
            )
        }
    }

    private suspend fun add(
        icons: Map<DrawableResource, Painter>,
        getId: (DrawableResource) -> String?,
        create: suspend (DrawableResource, Painter) -> ResolvedStyleImage,
    ) {
        snapshotFlow { map.style.loadState }.first { it == StyleLoadState.Ready }
        mutex.withLock {
            for ((icon, painter) in icons) {
                val id = getId(icon) ?: continue
                if (map.style.images[id] != null) continue
                val image = create(icon, painter)
                try {
                    map.style.images.set(id, image)
                } catch (e: StyleHandleException) {
                    // the style was replaced meanwhile; the next call adds the image again
                    return
                }
            }
        }
    }

    /** The painter's intrinsic size, at most [maxSize] wide and high */
    private fun Painter.sizeAtMost(maxSize: Dp): DpSize = with(density) {
        val maxPx = maxSize.toPx()
        DpSize(
            min(intrinsicSize.width.takeIf { it.isFinite() } ?: maxPx, maxPx).toDp(),
            min(intrinsicSize.height.takeIf { it.isFinite() } ?: maxPx, maxPx).toDp(),
        )
    }

    private companion object {
        val PIN_SIZE = 71.dp
        val MAX_ICON_SIZE = 48.dp
        /** same as the halo of labels on the map */
        val ICON_HALO_WIDTH = 2.5.dp
    }
}

/** Prefix of the names of icons added with [MapImages.addIcons] in the given colors. The colors
 *  are part of the name because they are drawn into the image. */
fun iconNamePrefix(color: Color, haloColor: Color): String =
    "icon_${color.toArgb().toUInt().toString(16)}_${haloColor.toArgb().toUInt().toString(16)}_"
