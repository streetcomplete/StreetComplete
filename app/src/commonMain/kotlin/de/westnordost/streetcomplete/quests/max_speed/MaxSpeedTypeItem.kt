package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.ktx.conditional
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.util.ktx.livingStreetSignDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun MaxSpeedTypeItem(
    maxSpeedType: MaxSpeedType,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        MaxSpeedTypeIcon(
            maxSpeedType = maxSpeedType,
            countryInfo = countryInfo
        )
        Text(
            text = stringResource(maxSpeedType.text),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption.copy(hyphens = Hyphens.Auto),
            modifier = Modifier.padding(2.dp)
        )
    }
}

/** Icon for max speed type selection. This isn't just a (painter) resource because the max speed
 *  zone sign icon is actually another composable layout*/
@Composable
private fun MaxSpeedTypeIcon(
    maxSpeedType: MaxSpeedType,
    countryInfo: CountryInfo,
    modifier: Modifier = Modifier,
) {
    when (maxSpeedType) {
        MaxSpeedType.SIGN -> {
            Image(
                painter = painterResource(when (countryInfo.countryCode) {
                    "CA" ->             Res.drawable.maxspeed_sign_ca
                    "US" ->             Res.drawable.maxspeed_sign_yellow
                    "FI", "IS", "SE" -> Res.drawable.maxspeed_sign_yellow
                    else ->             Res.drawable.maxspeed_sign
                }),
                contentDescription = null
            )
        }
        MaxSpeedType.ZONE -> {
            val backgroundColor = when(countryInfo.countryCode) {
                "FI", "IS", "SE" -> TrafficSignColor.Yellow
                else -> TrafficSignColor.White
            }
            MaxSpeedZoneSignIcon(
                zoneLabel = countryInfo.slowZoneLabelText ?: "ZONE",
                zoneIsAtTop = countryInfo.slowZoneLabelPosition == "top",
                backgroundColor = backgroundColor
            )
        }
        MaxSpeedType.LIVING_STREET -> {
            Image(
                painter = painterResource(countryInfo.livingStreetSignDrawable),
                contentDescription = null
            )
        }
        MaxSpeedType.ADVISORY -> {
            Image(
                painter = painterResource(when (countryInfo.advisorySpeedLimitSignStyle) {
                    "yellow" -> Res.drawable.maxspeed_advisory_sign_yellow
                    "white" ->  Res.drawable.maxspeed_advisory_sign_white
                    "blue" ->   Res.drawable.maxspeed_advisory_sign
                    else ->     Res.drawable.maxspeed_advisory_sign
                }),
                contentDescription = null
            )
        }
        MaxSpeedType.NO_SIGN -> {
            // just empty space!
            Box(Modifier.size(128.dp))
        }
        MaxSpeedType.NSL -> {
            Image(
                painter = painterResource(Res.drawable.national_speed_limit),
                contentDescription = null
            )
        }
    }
}

private val MaxSpeedType.text get() = when (this) {
    MaxSpeedType.SIGN -> Res.string.quest_maxspeed_answer_sign
    MaxSpeedType.ZONE -> Res.string.quest_maxspeed_answer_zone2
    MaxSpeedType.LIVING_STREET -> Res.string.quest_maxspeed_answer_living_street
    MaxSpeedType.ADVISORY -> Res.string.quest_maxspeed_answer_advisory_speed_limit
    MaxSpeedType.NO_SIGN -> Res.string.quest_maxspeed_answer_noSign2
    MaxSpeedType.NSL -> Res.string.quest_maxspeed_answer_nsl
}
