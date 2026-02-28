package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.ui.common.ProhibitorySign
import de.westnordost.streetcomplete.ui.common.RectangularSign
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.ui.theme.headlineLarge

/** Max speed zone input, resembling (somewhat) the speed limit zone sign in the given country.
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
 * */
@Composable
fun MaxSpeedZoneInput(
    maxSpeedZone: MaxSpeedZone?,
    onMaxSpeedZone: (MaxSpeedZone?) -> Unit,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    MaxSpeedZoneSign(
        zoneLabel = countryInfo.slowZoneLabelText ?: "ZONE",
        zoneIsAtTop = countryInfo.slowZoneLabelPosition == "top",
        modifier = modifier,
        backgroundColor = when (countryInfo.countryCode) {
            "IL" ->                   TrafficSignColor.Blue
            "FI", "IS", "SE" ->       TrafficSignColor.Yellow
            // https://commons.wikimedia.org/wiki/File:Luxembourg_road_sign_H,1-1.svg
            "LU" ->                   TrafficSignColor.Yellow
            else ->                   TrafficSignColor.White
        },
        color = when (countryInfo.countryCode) {
            "FI", "IS", "SE" -> TrafficSignColor.Yellow
            else ->             TrafficSignColor.White
        },
    ) {
        SpeedInput(
            speed = maxSpeedZone?.speed,
            onSpeedChange = { speed ->
                onMaxSpeedZone(speed?.let { MaxSpeedZone(it) })
            },
            selectableUnits = countryInfo.speedUnits,
        )
    }
}

/** Surface that looks like generic a max speed zone sign (rectangle with a prohibitory sign in the
 *  middle and a label above or below) */
@Composable
private fun MaxSpeedZoneSign(
    zoneLabel: String?,
    zoneIsAtTop: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = TrafficSignColor.White,
    color: Color = TrafficSignColor.White,
    content: @Composable BoxScope.() -> Unit,
) {
    RectangularSign(
        modifier = modifier.size(256.dp),
        color = backgroundColor
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(8.dp)
        ) {
            val textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
            if (zoneLabel != null && zoneIsAtTop) Text(zoneLabel, style = textStyle)

            ProhibitorySign(
                modifier = Modifier.weight(1f),
                color = color,
                content = content
            )

            if (zoneLabel != null && !zoneIsAtTop) Text(zoneLabel, style = textStyle)
        }
    }
}
