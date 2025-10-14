package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.VerticalDivider

/** Content for the last picked chip for the lanes */
@Composable
fun LanesButtonContent(
    laneCount: Int,
    modifier: Modifier = Modifier,
    rotation: Float = 0f,
) {
    Box(
        modifier = modifier.rotate(rotation),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            ProvideTextStyle(MaterialTheme.typography.body2.copy(fontWeight = FontWeight.Bold)) {

            Text(
                text = laneCount.toString(),
                modifier = Modifier.rotate(-rotation)
            )
            VerticalDivider()
            Text(
                text = laneCount.toString(),
                modifier = Modifier.rotate(-rotation)
            )
            }
        }
    }
}
