package de.westnordost.streetcomplete.screens.main.teammode

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.ktx.dpToSp
import de.westnordost.streetcomplete.ui.theme.TeamColors

/** Circle showing the color and letter of the selected team mode index.
 *  A size should be provided, as it has no intrinsic size*/
@Composable
fun TeamModeColorCircle(
    index: Int,
    modifier: Modifier = Modifier,
) {
    val color = TeamColors[index]

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .background(color, CircleShape)
            .aspectRatio(1f)
    ) {
        Text(
            text = (index + 'A'.code).toChar().toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            fontSize = (maxWidth * 0.5f).dpToSp()
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
private fun PreviewTeamModeColorCircle() {
    FlowRow {
        for (index in TeamColors.indices) {
            TeamModeColorCircle(index = index, modifier = Modifier.size(24.dp))
        }
    }
}
