package de.westnordost.streetcomplete.quests.lanes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import de.westnordost.streetcomplete.osm.oneway.isOneway
import de.westnordost.streetcomplete.osm.oneway.isReversedOneway
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddLanesForm : AbstractOsmQuestForm<LanesAnswer>() {

    @Composable
    override fun Content() {
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

        QuestForm(
            answers = Confirm(
                isComplete =
                    if (!isOneway) {
                        answer.forward != null && answer.backward != null
                    } else {
                        answer.forward != null || answer.backward != null
                    },
                hasChanges = answer.forward != null || answer.backward != null
            ) {
                applyAnswer(answer)
            },
            otherAnswers = buildList {
                if (!isOneway && countryInfo.hasCenterLeftTurnLane) {
                    add(Answer(stringResource(Res.string.quest_lanes_answer_lanes_center_left_turn_lane)) {
                        answer = answer.copy(centerLeftTurnLane = true)
                    })
                }
                add(Answer(stringResource(Res.string.quest_lanes_answer_noLanes)) {
                    applyAnswer(LanesAnswer.IsUnmarked)
                })
            },
            contentPadding = PaddingValues.Zero,
        ) {
            LanesForm(
                value = answer,
                onValueChanged = { answer = it },
                wayRotation = geometryRotation.floatValue,
                mapRotation = mapRotation.floatValue,
                mapTilt = mapTilt.floatValue,
                isOneway = isOneway,
                isReversedOneway = isReversedOneway,
                isLeftHandTraffic = countryInfo.isLeftHandTraffic,
                centerLineColor = centerLineColor,
                edgeLineColor = edgeLineColor,
                edgeLineStyle = edgeLineStyle,
            )
        }
    }
}
