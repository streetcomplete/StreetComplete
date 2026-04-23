package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.trafficSignContentColorFor
import androidx.compose.ui.tooling.preview.Preview

/** A surface in the appearance of a rectangular traffic sign but with inverted colors. */
@Composable
fun InvertedRectangularSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.Black,
    content: @Composable BoxScope.() -> Unit
) {
    val contentColor = trafficSignContentColorFor(color)
    Box(
        modifier = modifier
            .border(Dp.Hairline, Color.LightGray, RoundedCornerShape(10.dp))
            .background(color, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .background(contentColor, RoundedCornerShape(6.dp))
            .padding(4.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides color) {
            content()
        }
    }
}

@Composable @Preview
private fun RectangularSignWhitePreview() {
    InvertedRectangularSign { Text("a") }
}

@Composable @Preview
private fun RectangularSignYellowPreview() {
    InvertedRectangularSign(color = TrafficSignColor.Yellow) { Text("b") }
}

@Composable @Preview
private fun RectangularSignBluePreview() {
    InvertedRectangularSign(color = TrafficSignColor.Blue) { Text("c") }
}
