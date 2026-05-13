package de.westnordost.streetcomplete.quests.charging_station_bicycles

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.charging_station_bicycles.ChargingStationBicycles.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddChargingStationBicyclesForm(
    onAnswer: (ChargingStationBicycles) -> Unit,
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) },
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_charging_station_bicycles_answer_only)) { onAnswer(ONLY) }
        )
    )
}
