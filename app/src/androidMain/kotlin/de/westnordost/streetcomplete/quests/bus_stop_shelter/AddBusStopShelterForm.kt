package de.westnordost.streetcomplete.quests.bus_stop_shelter

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.bus_stop_shelter.BusStopShelterAnswer.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddBusStopShelterForm(
    onAnswer: (BusStopShelterAnswer) -> Unit,
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO_SHELTER) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(SHELTER) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_busStopShelter_covered)) { onAnswer(COVERED) }
        )
    )
}
