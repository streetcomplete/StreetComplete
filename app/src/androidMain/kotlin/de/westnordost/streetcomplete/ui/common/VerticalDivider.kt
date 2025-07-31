package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.onSurface.copy(alpha = DividerAlpha),
    thickness: Dp = 1.dp
) {
    val targetThickness =
        if (thickness == Dp.Hairline) {
            (1f / LocalDensity.current.density).dp
        } else {
            thickness
        }
    Box(modifier.fillMaxHeight().width(targetThickness).background(color = color))
}

private const val DividerAlpha = 0.12f
