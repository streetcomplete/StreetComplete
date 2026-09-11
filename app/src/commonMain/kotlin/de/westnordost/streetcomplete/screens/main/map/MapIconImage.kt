package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.id
import de.westnordost.streetcomplete.ui.util.ColorFilterPainter
import de.westnordost.streetcomplete.ui.util.WithHaloPainter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.ImageValue

/** Icons are at most 48 dp plus their halo; only monochrome preset icons are tinted. */
@Composable
internal fun mapIconImage(
    icon: DrawableResource,
    color: Color,
    haloColor: Color
): Expression<ImageValue> {
    val painter = painterResource(icon)
    val density = LocalDensity.current
    val haloWidth = 2.5.dp
    val haloPainter = remember(icon, painter, density, color, haloColor) {
        val tintedPainter = if (icon.id?.startsWith("preset_") == true) {
            ColorFilterPainter(painter, ColorFilter.tint(color))
        } else painter
        WithHaloPainter(tintedPainter, density, haloWidth, haloColor)
    }
    val size = with(density) {
        DpSize(
            painter.intrinsicSize.width.toDp().coerceAtMost(48.dp) + haloWidth * 2,
            painter.intrinsicSize.height.toDp().coerceAtMost(48.dp) + haloWidth * 2,
        )
    }
    return image(
        haloPainter,
        size = size,
    )
}
