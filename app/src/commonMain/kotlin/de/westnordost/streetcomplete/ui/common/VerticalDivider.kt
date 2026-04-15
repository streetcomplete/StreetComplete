package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.divider

/** Same as a Divider, only vertical. (In Material3, this is already available.) */
@Composable
fun VerticalDivider(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.divider,
    thickness: Dp = 1.dp
) {
    Canvas(modifier.fillMaxHeight().width(thickness)) {
        val width = thickness.toPx()
        val offset = width / 2f
        drawLine(
            color = color,
            strokeWidth = width,
            start = Offset(offset, 0f),
            end = Offset(offset, size.height),
        )
    }
}

