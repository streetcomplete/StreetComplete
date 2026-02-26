package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Surface that looks like a standard advisory max speed sign */
@Composable
fun AdvisoryMaxSpeedSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.Blue,
    content: @Composable BoxScope.() -> Unit,
) {
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)) {
        RectangularSign(
            modifier = modifier.size(160.dp),
            color = color,
            content = content
        )
    }
}
