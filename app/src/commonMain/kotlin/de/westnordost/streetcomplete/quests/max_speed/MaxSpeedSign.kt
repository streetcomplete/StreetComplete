package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.ktx.dpToSp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.extraLargeInput

/** Surface resembling (somewhat) the speed limit sign in the given country:
 *
 *  For US and Canada, we have these rectangular speed limit signs. Some countries use a yellow
 *  background on the standard (speed limit) signs
 * */
@Composable
fun MaxSpeedSign(
    countryCode: String,
    modifier: Modifier = Modifier,
    content: @Composable (Shape) -> Unit,
) {
    when (countryCode) {
        "CA", "US" -> MaxSpeedSignMutcd(countryCode, modifier) { content(RoundedCornerShape(4.dp)) }
        else ->       MaxSpeedSignDefault(countryCode, modifier) { content(CircleShape) }
    }
}

/** Surface that looks like a standard max speed sign (white circle with red border) */
@Composable
private fun MaxSpeedSignDefault(
    countryCode: String,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    ProhibitorySign(
        modifier = modifier.size(144.dp),
        color = when (countryCode) {
            "FI", "IS", "SE" -> TrafficSignColor.Yellow
            else ->             TrafficSignColor.White
        },
        borderWidth = 20.dp,
        content = content
    )
}

/** Surface that looks like a max speed sign in (a few) MUTCD countries */
@Composable
private fun MaxSpeedSignMutcd(
    countryCode: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    RectangularSign(modifier.width(128.dp)) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(4.dp),
        ) {
            BasicText(
                text = when (countryCode) {
                    "CA" -> "MAXIMUM"
                    "US" -> "SPEED\nLIMIT"
                    else -> ""
                },
                style = TextStyle.Default.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                autoSize = TextAutoSize.StepBased(maxFontSize = 28.dp.dpToSp()),
                softWrap = false,
                modifier = Modifier.fillMaxWidth(),
            )
            content()
        }
    }
}
