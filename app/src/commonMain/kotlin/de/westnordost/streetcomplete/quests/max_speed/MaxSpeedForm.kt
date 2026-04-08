package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_THAT_MAY_BE_LIVING_STREETS
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WHERE_SLOW_ZONE_IS_NOT_POSSIBLE
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WITH_DEFINITE_SPEED_LIMIT
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.DropdownButton
import de.westnordost.streetcomplete.util.ktx.livingStreetSignDrawable
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** Form to select the max speed and how it is defined */
@Composable
fun MaxSpeedForm(
    countryInfo: CountryInfo,
    highwayValue: String,
    answer: MaxSpeedAnswer?,
    onAnswer: (MaxSpeedAnswer?) -> Unit,
    modifier: Modifier = Modifier,
    initialZoneSpeedValue: Int? = null,
) {
    val selectableMaxSpeedTypes = remember(countryInfo) {
        val speedUnit = countryInfo.speedUnits.first()
        val initialSpeed = Speed(null, speedUnit)
        buildList {
            add(MaxSpeedSign(initialSpeed, MaxSpeedSign.Type.NORMAL))
            // we only need to ask the user for the road type if the current road is not
            // assumed to have always a definite default speed limit, because then we can
            // tag e.g. maxspeed:type=XX:motorway as "road type" to denote that the default
            // speed limit is in effect
            if (highwayValue in ROADS_WITH_DEFINITE_SPEED_LIMIT) {
                add(MaxSpeedAnswer.NoSign)
            } else {
                add(MaxSpeedAnswer.NoSignWithRoadType(null))
            }
            if (countryInfo.hasSlowZone && highwayValue !in ROADS_WHERE_SLOW_ZONE_IS_NOT_POSSIBLE) {
                add(MaxSpeedSign(Speed(initialZoneSpeedValue, speedUnit), MaxSpeedSign.Type.ZONE))
            }
            if (countryInfo.hasLivingStreet && highwayValue in ROADS_THAT_MAY_BE_LIVING_STREETS) {
                add(MaxSpeedAnswer.IsLivingStreet)
            }
            if (countryInfo.hasAdvisorySpeedLimitSign && (answer as? MaxSpeedSign)?.type == MaxSpeedSign.Type.ADVISORY) {
                add(MaxSpeedSign(initialSpeed, MaxSpeedSign.Type.ADVISORY))
            }
        }
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
            onSelectedItem = { onAnswer(it) },
            selectedItem = answer,
            itemContent = { Text(stringResource(it.text)) }
        )
        AnimatedContent(
            targetState = answer,
            // only do any animation when the maxspeed type changes!
            contentKey = { it?.let { it::class }},
            contentAlignment = Alignment.TopCenter
        ) { answer ->
            when (answer) {
                is MaxSpeedSign -> {
                    MaxSpeedInput(
                        type = answer.type,
                        speed = answer.speed,
                        onChangeSpeed = { onAnswer(answer.copy(speed = it)) },
                        countryInfo = countryInfo,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // literally every living street sign in every country looks different, however,
                // they look somewhat similar and can be grouped into different categories that look
                // at least alike the actual sign. So, we do that here
                MaxSpeedAnswer.IsLivingStreet -> Image(
                    painter = painterResource(countryInfo.livingStreetSignDrawable),
                    contentDescription = null,
                )
                is MaxSpeedAnswer.NoSignWithRoadType -> {
                    RoadTypeSelect(
                        roadType = answer.roadType,
                        onRoadType = { onAnswer(it?.let { MaxSpeedAnswer.NoSignWithRoadType(it) }) },
                        countryCode = countryInfo.countryCode,
                    )
                }
                MaxSpeedAnswer.NoSign, null -> { /* nothing */ }
            }
        }
    }
}
