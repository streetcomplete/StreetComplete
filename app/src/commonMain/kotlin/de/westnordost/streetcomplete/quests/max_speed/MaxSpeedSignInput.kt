package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.SpeedMeasurementUnit
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.theme.largeInput

/** Max speed input, resembling (somewhat) the speed limit sign in the given country:
 *
 *  For US and Canada, we have these rectangular speed limit signs. Some countries use a yellow
 *  background on the standard (speed limit) signs
 * */
@Composable
fun MaxSpeedSignInput(
    maxSpeedSign: MaxSpeedSign?,
    onMaxSpeedSign: (MaxSpeedSign?) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    val speedInputComposable = @Composable {
        SpeedInput(
            speed = maxSpeedSign?.value,
            onSpeedChange = { speed -> onMaxSpeedSign(speed?.let { MaxSpeedSign(it) }) },
            selectableUnits = countryInfo.speedUnits,
        )
    }
    when (countryInfo.countryCode) {
        "CA" -> MaxSpeedSignMutcd("MAXIMUM", modifier = modifier) { speedInputComposable() }
        "US" -> MaxSpeedSignMutcd("SPEED LIMIT", modifier = modifier) { speedInputComposable() }
        else -> MaxSpeedSign(
            modifier = modifier,
            color = when (countryInfo.countryCode) {
                "FI", "IS", "SE" -> TrafficSignColor.Yellow
                else -> TrafficSignColor.White
            },
        ) { speedInputComposable() }
    }
}

/** Surface that looks like a standard max speed sign (white circle with red border) */
@Composable
private fun MaxSpeedSign(
    modifier: Modifier = Modifier,
    color: Color = TrafficSignColor.White,
    content: @Composable BoxScope.() -> Unit,
) {
    ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(fontWeight = FontWeight.Bold)) {
        ProhibitorySign(
            modifier = modifier.size(192.dp),
            color = color,
            content = content
        )
    }
}

/** Surface that looks like a max speed sign in (a few) MUTCD countries */
@Composable
private fun MaxSpeedSignMutcd(
    text: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RectangularSign(modifier.width(160.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(4.dp),
        ) {
            ProvideTextStyle(MaterialTheme.typography.largeInput.copy(fontWeight = FontWeight.Bold)) {
                Text(text)
            }
            ProvideTextStyle(MaterialTheme.typography.extraLargeInput.copy(
                fontWeight = FontWeight.Bold
            )) {
                content()
            }
        }
    }
}
