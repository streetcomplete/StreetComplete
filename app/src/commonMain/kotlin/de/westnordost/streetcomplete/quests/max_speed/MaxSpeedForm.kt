package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cheonjaeung.compose.grid.SimpleGridCells
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.meta.IncompleteCountryInfo
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.ic_arrow_drop_down_24
import de.westnordost.streetcomplete.resources.quest_maxspeed_type_description
import de.westnordost.streetcomplete.resources.quest_select_hint
import de.westnordost.streetcomplete.ui.common.Button2
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.ui.common.dialogs.SimpleItemSelectDialog
import de.westnordost.streetcomplete.ui.theme.TrafficSignColor
import de.westnordost.streetcomplete.util.ktx.livingStreetSignDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to select the max speed and how it is defined */
@Composable
fun MaxSpeedForm(
    countryInfo: CountryInfo,
    onMaxSpeed: (MaxSpeedAnswer) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSelectionDialog by remember { mutableStateOf(false) }
    var selectedMaxSpeedType by remember { mutableStateOf<MaxSpeedType?>(null) }
    val selectableMaxSpeedTypes = remember(countryInfo) { buildList {
        add(MaxSpeedType.SIGN)
        add(MaxSpeedType.NO_SIGN)
        if (countryInfo.hasSlowZone) add(MaxSpeedType.ZONE)
        if (countryInfo.hasLivingStreet) add(MaxSpeedType.LIVING_STREET)
        if (countryInfo.hasAdvisorySpeedLimitSign) add(MaxSpeedType.ADVISORY)
    } }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(Res.string.quest_maxspeed_type_description),
            color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
            style = MaterialTheme.typography.body2,
        )
        DropdownButton(
            items = selectableMaxSpeedTypes,
            onSelectedItem = { selectedMaxSpeedType = it },
            itemContent = { Text(stringResource(it.text)) }
        )
        selectedMaxSpeedType?.let { selectedMaxSpeedType ->
            MaxSpeedTypeForm(
                maxSpeedType = selectedMaxSpeedType,
                countryInfo = countryInfo,
                speedInput = {
                    // TODO
                },
                defaultSpeedInput = {
                    // TODO
                }
            )
        }
    }
}

/** Form to select the max speed */
@Composable
private fun MaxSpeedTypeForm(
    maxSpeedType: MaxSpeedType,
    countryInfo: CountryInfo,
    speedInput: @Composable () -> Unit,
    defaultSpeedInput: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val test = @Composable { Text("asdf") }
    when (maxSpeedType) {
        // the sign appearance is customized a bit to look a bit more like the actual sign in that
        // country: For US and Canada, we have these rectangular speed limit signs. Some
        // countries use a yellow background on the standard (speed limit) signs
        MaxSpeedType.SIGN -> when (countryInfo.countryCode) {
            "CA" -> MaxSpeedSignMutcd("MAXIMUM", modifier = modifier) { speedInput() }
            "US" -> MaxSpeedSignMutcd("SPEED LIMIT", modifier = modifier) { speedInput() }
            else -> MaxSpeedSign(
                modifier = modifier,
                color = when (countryInfo.countryCode) {
                    "FI", "IS", "SE" -> TrafficSignColor.Yellow
                    else -> TrafficSignColor.White
                },
            ) { speedInput() }
        }
        // there is quite a bit of customization at play here to make the sign look more alike how
        // the actual sign looks like in that country:
        // Some have the "ZONE" label at the top, some at the bottom, some don't have any label at
        // all and some use different colors. The slow zone sign in Japan and in Mexico deviate a
        // bit more from the default appearance, but should still be somewhat recognizable.
        // (Japan: Normal speed limit sign with a small extra sign below, Mexico: Zone label is
        // within the speed limit sign rather than the rectangular sign around it)
        //
        // (Fortunately,) US and Canada don't really have speed limit zones, so we don't need to
        // worry about yet another MUTCD-inspired speed zone layout
        MaxSpeedType.ZONE -> {
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
                    else -> TrafficSignColor.White
                },
            ) { speedInput() }
        }
        // the sign appearance is customized a bit to look a bit more like the actual sign in that
        // country: They are all more or less rectangular, so that adapting the color is a close-
        // enough approximation, even for US and Canada.
        MaxSpeedType.ADVISORY -> {
            AdvisoryMaxSpeedSign(
                color = when (countryInfo.advisorySpeedLimitSignStyle) {
                    "yellow" -> TrafficSignColor.Yellow
                    "white" -> TrafficSignColor.White
                    "blue" -> TrafficSignColor.Blue
                    else -> TrafficSignColor.Blue
                },
                modifier = modifier,
            ) { speedInput() }
        }
        // literally every living street sign in every country looks different, however, they look
        // somewhat similar and can be grouped into different categories that look at least alike
        // the actual sign. So, we do that here
        MaxSpeedType.LIVING_STREET -> {
            Image(
                painter = painterResource(countryInfo.livingStreetSignDrawable),
                contentDescription = null,
                modifier = modifier,
            )
        }
        MaxSpeedType.NO_SIGN -> {
            Box(modifier) {
                defaultSpeedInput()
            }
        }
    }
}

@Preview
@Composable
private fun MaxSpeedFormPreview() {
    MaxSpeedForm(
        CountryInfo(listOf(
            IncompleteCountryInfo(
                countryCode = "DE",
                advisorySpeedLimitSignStyle = "blue",
                hasAdvisorySpeedLimitSign = true,
                hasSlowZone = true,
                slowZoneLabelPosition = "bottom",
                slowZoneLabelText = "ZÖNI",
                hasLivingStreet = true,
                livingStreetSignStyle = "columbia"
            )
        )), {}
    )
}
