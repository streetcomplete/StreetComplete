package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.TrafficWhite
import de.westnordost.streetcomplete.ui.theme.trafficContentColorFor

/** A surface in the appearance of a rectangular traffic sign. */
@Composable
fun RectangularSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficWhite,
    content: @Composable () -> Unit
) {
    val contentColor = trafficContentColorFor(color)
    Box(
        modifier = modifier
            .background(color, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .border(4.dp, contentColor, RoundedCornerShape(6.dp))
            .padding(16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}
