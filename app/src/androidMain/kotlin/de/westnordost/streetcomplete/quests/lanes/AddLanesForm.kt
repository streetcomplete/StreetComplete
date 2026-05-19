package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.LocalMapRotation
import de.westnordost.streetcomplete.ui.common.quest.LocalMapTilt
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import de.westnordost.streetcomplete.util.math.getOrientationOrZero
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddLanesForm(
    onAnswer: (LanesAnswer) -> Unit,
    element: Element,
    geometry: ElementGeometry,
    countryInfo: CountryInfo
) {
    var answer by rememberSerializable { mutableStateOf(Lanes()) }

    val edgeLineStyle = remember {
        when {
            countryInfo.edgeLineStyle.contains("short dashes") -> LineStyle.SHORT_DASHES
            countryInfo.edgeLineStyle.contains("dashes") -> LineStyle.DASHES
            else -> LineStyle.CONTINUOUS
        }
    }
    val edgeLineColor = remember {
        if (countryInfo.edgeLineStyle.contains("yellow")) Color.Yellow else Color.White
    }
    val centerLineColor = remember {
        if (countryInfo.centerLineStyle.contains("yellow")) Color.Yellow else Color.White
    }
    val isOneway = remember { isOneway(element.tags) }
    val isReversedOneway = remember { isReversedOneway(element.tags) }
    val geometryRotation = remember(geometry) { geometry.getOrientationOrZero() }

    QuestForm(
        isComplete =
            if (!isOneway) {
                answer.forward != null && answer.backward != null
            } else {
                answer.forward != null || answer.backward != null
            },
        hasChanges = answer.forward != null || answer.backward != null,
        onClickOk =  { onAnswer(answer) },
        otherAnswers = listOfNotNull(
            if (!isOneway && countryInfo.hasCenterLeftTurnLane) {
                Answer(stringResource(Res.string.quest_lanes_answer_lanes_center_left_turn_lane)) {
                    answer = answer.copy(centerLeftTurnLane = true)
                }
            } else null,
            Answer(stringResource(Res.string.quest_lanes_answer_noLanes)) {
                onAnswer(LanesAnswer.IsUnmarked)
            }
        ),
        contentPadding = PaddingValues.Zero,
    ) {
        LanesForm(
            value = answer,
            onValueChanged = { answer = it },
            wayRotation = geometryRotation,
            mapRotation = LocalMapRotation.current,
            mapTilt = LocalMapTilt.current,
            isOneway = isOneway,
            isReversedOneway = isReversedOneway,
            isLeftHandTraffic = countryInfo.isLeftHandTraffic,
            centerLineColor = centerLineColor,
            edgeLineColor = edgeLineColor,
            edgeLineStyle = edgeLineStyle,
        )
    }
}
