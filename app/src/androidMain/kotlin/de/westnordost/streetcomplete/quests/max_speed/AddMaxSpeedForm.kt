package de.westnordost.streetcomplete.quests.max_speed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.osm.maxspeed.ROADS_WHERE_SLOW_ZONE_IS_LIKELY
import de.westnordost.streetcomplete.osm.maxspeed.Speed
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.max_speed.MaxSpeedSign.Type.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.dialogs.QuestConfirmationDialog
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Confirm
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import de.westnordost.streetcomplete.ui.theme.extraLargeInput
import de.westnordost.streetcomplete.ui.util.rememberSerializable
import org.jetbrains.compose.resources.stringResource

class AddMaxSpeedForm : AbstractOsmQuestForm<MaxSpeedAnswer>() {

    @Composable
    override fun Content() {
        var maxSpeedAnswer by rememberSerializable { mutableStateOf<MaxSpeedAnswer?>(null) }
        var confirmNoSign by remember { mutableStateOf(false) }
        var confirmUnusualInput by remember { mutableStateOf(false) }
        var confirmNoSignSlowZone by remember { mutableStateOf(false) }

        val advisorySpeedLimitAnswer = Answer(stringResource(Res.string.quest_maxspeed_answer_advisory_speed_limit)) {
            maxSpeedAnswer = MaxSpeedSign(Speed(null, countryInfo.speedUnits.first()), ADVISORY)
        }

        QuestForm(
            answers = Confirm(isComplete = maxSpeedAnswer?.isComplete() == true) {
                if (maxSpeedAnswer is MaxSpeedAnswer.NoSignWithRoadType) {
                    if (countryInfo.hasSlowZone && element.tags["highway"] in ROADS_WHERE_SLOW_ZONE_IS_LIKELY) {
                        confirmNoSignSlowZone = true
                    } else {
                        confirmNoSign = true
                    }
                } else if ((maxSpeedAnswer as? MaxSpeedSign)?.isUnusualSpeed() == true) {
                    confirmUnusualInput = true
                } else {
                    maxSpeedAnswer?.let { applySpeedLimitFormAnswer(it) }
                }
            },
            otherAnswers = buildList {
                if (countryInfo.hasAdvisorySpeedLimitSign) {
                    add(advisorySpeedLimitAnswer)
                }
            }
        ) {
            MaxSpeedForm(
                countryInfo = countryInfo,
                highwayValue = element.tags["highway"]!!,
                answer = maxSpeedAnswer,
                onAnswer = { maxSpeedAnswer = it },
                initialZoneSpeedValue = LAST_INPUT_SLOW_ZONE,
            )
        }

        if (confirmNoSign) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSign = false },
                onConfirmed = { maxSpeedAnswer?.let { applySpeedLimitFormAnswer(it) } },
                titleText = stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation_title),
                text = { Text(stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation)) },
                confirmButtonText = stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation_positive)
            )
        }
        if (confirmUnusualInput) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmUnusualInput = false },
                onConfirmed = { maxSpeedAnswer?.let { applySpeedLimitFormAnswer(it) } },
                text = { Text(stringResource(Res.string.quest_maxspeed_unusualInput_confirmation_description)) }
            )
        }
        if (confirmNoSignSlowZone) {
            QuestConfirmationDialog(
                onDismissRequest = { confirmNoSignSlowZone = false },
                onConfirmed = { maxSpeedAnswer?.let { applySpeedLimitFormAnswer(it) } },
                titleText = stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation_title),
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation))
                        Text(stringResource(Res.string.quest_maxspeed_answer_noSign_info_zone))
                        MaxSpeedZoneSign(countryInfo = countryInfo) {
                            Text(
                                text = "××",
                                style = MaterialTheme.typography.extraLargeInput.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                },
                confirmButtonText = stringResource(Res.string.quest_maxspeed_answer_noSign_confirmation_positive)
            )
        }
    }

    private fun applySpeedLimitFormAnswer(answer: MaxSpeedAnswer) {
        val maxSpeedSign = answer as? MaxSpeedSign
        if (maxSpeedSign?.type == ZONE) {
            LAST_INPUT_SLOW_ZONE = maxSpeedSign.speed.value
        }
        applyAnswer(answer)
    }

    /* ----------------------------------------- No sign ---------------------------------------- */

    companion object {
        private var LAST_INPUT_SLOW_ZONE: Int? = null
    }
}
