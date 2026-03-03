package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.ktx.dpToSp
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.trafficSignContentColorFor

/** Surface that looks like generic a max speed zone sign (rectangle with a prohibitory sign in the
 *  middle and a label above or below)
 *
 *  There is more variation here than for the max speed sign. We have quite a bit of customization
 *  here to make the sign look more alike how the actual sign looks like in that country:
 *
 *  Some have the "ZONE" label at the top, some at the bottom, some don't have any label at all and
 *  some use different colors.
 *  The slow zone sign in Japan and in Mexico deviate a bit more from the default appearance, but
 *  should still be somewhat recognizable. (Japan: Normal speed limit sign with a small extra sign
 *  below, Mexico: Zone label is within the speed limit sign rather than the rectangular sign around
 *  it)
 *
 *  (Fortunately,) US and Canada don't really have speed limit zones, so we don't need to worry yet
 *  yet another MUTCD-inspired speed zone layout
 *  */
@Composable
fun MaxSpeedZoneSign(
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
    content: @Composable (Shape) -> Unit,
) {
    val zoneLabel = countryInfo.slowZoneLabelText
    val zoneIsAtTop = countryInfo.slowZoneLabelPosition == "top"

    RectangularSign(
        modifier = modifier.size(192.dp),
        color = when (countryInfo.countryCode) {
            "IL" ->             TrafficSignColor.Blue
            "FI", "IS", "SE" -> TrafficSignColor.Yellow
            // https://commons.wikimedia.org/wiki/File:Luxembourg_road_sign_H,1-1.svg
            "LU" ->             TrafficSignColor.Yellow
            else ->             TrafficSignColor.White
        }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(4.dp)
        ) {
            val textStyle = TextStyle.Default.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.dp.dpToSp()
            )
            if (zoneLabel != null && zoneIsAtTop) Text(zoneLabel, style = textStyle)

            val color = when (countryInfo.countryCode) {
                "FI", "IS", "SE" -> TrafficSignColor.Yellow
                else ->             TrafficSignColor.White
            }
            val contentColor = trafficSignContentColorFor(color)
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .background(TrafficSignColor.Red, CircleShape)
                    .padding(18.dp)
                    .background(color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor) {
                    content(CircleShape)
                }
            }

            if (zoneLabel != null && !zoneIsAtTop) Text(zoneLabel, style = textStyle)
        }
    }
}
