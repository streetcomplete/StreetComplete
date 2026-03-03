package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor

/** Surface resembling (somewhat) the advisory speed limit sign in the given country:
 *  They are all more or less rectangular, so that adapting the color is a close-enough
 *  approximation, even for US and Canada. */
@Composable
fun AdvisorySpeedSign(
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    content: @Composable (Shape) -> Unit,
) {
    RectangularSign(
        modifier = modifier.width(144.dp),
        color = when (countryInfo.advisorySpeedLimitSignStyle) {
            "yellow" -> TrafficSignColor.Yellow
            "white" ->  TrafficSignColor.White
            "blue" ->   TrafficSignColor.Blue
            else ->     TrafficSignColor.Blue
        },
        content = { content(RoundedCornerShape(4.dp)) }
    )
}
