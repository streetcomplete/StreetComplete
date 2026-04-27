package de.westnordost.streetcomplete.quests.bus_stop_shelter

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.*
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddBusStopShelterForm : AbstractOsmQuestForm<BusStopShelterAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO_SHELTER) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(SHELTER) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_busStopShelter_covered)) { applyAnswer(COVERED) }
            )
        )
    }
}
