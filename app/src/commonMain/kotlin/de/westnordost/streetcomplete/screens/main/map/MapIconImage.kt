package de.westnordost.streetcomplete.screens.main.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.id
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.maplibre.compose.expressions.ast.Expression
import org.maplibre.compose.expressions.dsl.image
import org.maplibre.compose.expressions.value.ImageValue

/** Icons are at most 48 dp; monochrome preset icons can be tinted by their layer. */
@Composable
internal fun mapIconImage(icon: DrawableResource): Expression<ImageValue> {
    val painter = painterResource(icon)
    val size = with(LocalDensity.current) {
        DpSize(
            painter.intrinsicSize.width.toDp().coerceAtMost(48.dp),
            painter.intrinsicSize.height.toDp().coerceAtMost(48.dp),
        )
    }
    return image(
        painter,
        size = size,
        drawAsSdf = icon.id?.startsWith("preset_") == true,
    )
}
