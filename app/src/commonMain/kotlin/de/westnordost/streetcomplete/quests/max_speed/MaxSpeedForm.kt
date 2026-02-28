package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_THAT_MAY_BE_LIVING_STREETS
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WHERE_SLOW_ZONE_IS_NOT_POSSIBLE
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WITH_DEFINITE_SPEED_LIMIT
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_maxspeed_type_description
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.util.ktx.livingStreetSignDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to select the max speed and how it is defined */
@Composable
fun MaxSpeedForm(
    initialSelectedMaxSpeedType: MaxSpeedType?,
    countryInfo: CountryInfo,
    highwayValue: String,
    maxSpeed: MaxSpeedAnswer?,
    onMaxSpeed: (MaxSpeedAnswer?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectableMaxSpeedTypes = remember(countryInfo) { buildList {
        add(MaxSpeedType.SIGN)
        add(MaxSpeedType.DEFAULT)
        if (countryInfo.hasSlowZone && highwayValue !in ROADS_WHERE_SLOW_ZONE_IS_NOT_POSSIBLE) {
            add(MaxSpeedType.ZONE)
        }
        if (countryInfo.hasLivingStreet && highwayValue in ROADS_THAT_MAY_BE_LIVING_STREETS) {
            add(MaxSpeedType.LIVING_STREET)
        }
        // show only if initially selected (= from other answer)
        if (countryInfo.hasAdvisorySpeedLimitSign &&
            initialSelectedMaxSpeedType == MaxSpeedType.ADVISORY
        ) {
            add(MaxSpeedType.ADVISORY)
        }
    } }

    var selectedMaxSpeedType by remember(initialSelectedMaxSpeedType) {
        mutableStateOf<MaxSpeedType?>(initialSelectedMaxSpeedType)
    }

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
            when (selectedMaxSpeedType) {
                MaxSpeedType.SIGN -> MaxSpeedSignInput(
                    maxSpeedSign = maxSpeed as? MaxSpeedSign,
                    onMaxSpeedSign = onMaxSpeed,
                    countryInfo = countryInfo,
                    modifier = modifier,
                )
                MaxSpeedType.ZONE -> MaxSpeedZoneInput(
                    maxSpeedZone = maxSpeed as? MaxSpeedZone,
                    onMaxSpeedZone = onMaxSpeed,
                    countryInfo = countryInfo,
                    modifier = modifier,
                )
                MaxSpeedType.ADVISORY -> AdvisorySpeedSignInput(
                    advisorySpeedSign = maxSpeed as? AdvisorySpeedSign,
                    onAdvisorySpeedSign = onMaxSpeed,
                    countryInfo = countryInfo,
                    modifier = modifier,
                )
                // literally every living street sign in every country looks different, however,
                // they look somewhat similar and can be grouped into different categories that look
                // at least alike the actual sign. So, we do that here
                MaxSpeedType.LIVING_STREET -> Image(
                    painter = painterResource(countryInfo.livingStreetSignDrawable),
                    contentDescription = null,
                    modifier = modifier,
                )
                MaxSpeedType.DEFAULT -> {
                    // we only need to ask the user for the road type if the current road is not
                    // assumed to have always a definite default speed limit, because then we can
                    // tag e.g. maxspeed:type=XX:motorway as "road type" to denote that the default
                    // speed limit is in effect
                    if (highwayValue in ROADS_WITH_DEFINITE_SPEED_LIMIT) {
                        LaunchedEffect(selectedMaxSpeedType) {
                            onMaxSpeed(DefaultMaxSpeed(null))
                        }
                    } else {
                        RoadTypeSelect(
                            roadType = (maxSpeed as? DefaultMaxSpeed)?.roadType,
                            onRoadType = { onMaxSpeed(it?.let { DefaultMaxSpeed(it) }) },
                            countryCode = countryInfo.countryCode,
                            modifier = modifier,
                        )
                    }
                }
            }
        }
    }
}
