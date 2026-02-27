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
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Advisory speed input, resembling (somewhat) the advisory speed limit sign in the given country:
 *
 *  They are all more or less rectangular, so that adapting the color is a close-enough
 *  approximation, even for US and Canada.
 * */
@Composable
fun AdvisorySpeedSignInput(
    advisorySpeedSign: AdvisorySpeedSign?,
    onAdvisorySpeedSign: (AdvisorySpeedSign?) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    AdvisorySpeedSign(
        color = when (countryInfo.advisorySpeedLimitSignStyle) {
            "yellow" -> TrafficSignColor.Yellow
            "white" -> TrafficSignColor.White
            "blue" -> TrafficSignColor.Blue
            else -> TrafficSignColor.Blue
        },
        modifier = modifier,
    ) {
        SpeedInput(
            speed = advisorySpeedSign?.value,
            onSpeedChange = { speed ->
                onAdvisorySpeedSign(speed?.let { AdvisorySpeedSign(it) })
            },
            selectableUnits = countryInfo.speedUnits,
        )
    }
}

/** Surface that looks like a standard advisory max speed sign */
@Composable
private fun AdvisorySpeedSign(
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
