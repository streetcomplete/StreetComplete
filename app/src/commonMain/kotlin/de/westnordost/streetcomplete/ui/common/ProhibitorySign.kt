package de.westnordost.streetcomplete.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.trafficSignContentColorFor
import androidx.compose.ui.tooling.preview.Preview

/** A surface in the appearance of a prohibitory traffic sign (Vienna convention).
 *  Should define a fixed size.
 *  */
@Composable
fun ProhibitorySign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.White,
    content: @Composable BoxScope.() -> Unit
) {
    val contentColor = trafficSignContentColorFor(color)
    Box(
        modifier = modifier
            .border(Dp.Hairline, Color.LightGray, CircleShape)
            .background(color, CircleShape)
            .padding(4.dp)
            .background(TrafficSignColor.Red, CircleShape)
            .padding(32.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable @Preview
private fun ProhibitorySignPreview() {
    ProhibitorySign(Modifier.size(256.dp)) { Text("a") }
}
