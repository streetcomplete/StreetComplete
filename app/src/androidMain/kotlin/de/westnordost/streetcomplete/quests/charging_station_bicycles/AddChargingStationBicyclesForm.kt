package de.westnordost.streetcomplete.quests.charging_station_bicycles

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.charging_station_bicycles.ChargingStationBicycles.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddChargingStationBicyclesForm : AbstractOsmQuestForm<ChargingStationBicycles>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(YES) },
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_charging_station_bicycles_answer_only)) { applyAnswer(ONLY) }
            )
        )
    }
}
