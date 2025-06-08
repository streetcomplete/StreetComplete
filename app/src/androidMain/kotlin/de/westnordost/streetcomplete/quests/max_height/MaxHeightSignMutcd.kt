package de.westnordost.streetcomplete.quests.max_height

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.theme.TrafficBlack
import de.westnordost.streetcomplete.ui.theme.TrafficYellow

@Composable
fun MaxHeightSignMutcd(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .width(IntrinsicSize.Max)
            .height(IntrinsicSize.Min)
            .background(TrafficYellow, RoundedCornerShape(10.dp))
            .padding(4.dp)
            .border(4.dp, TrafficBlack, RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 16.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides TrafficBlack) {
            content()
        }
    }
}
