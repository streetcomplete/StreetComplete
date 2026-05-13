package de.westnordost.streetcomplete.quests.aerialway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.*
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddAerialwayBicycleAccessForm(
    onAnswer: (AerialwayBicycleAccessAnswer) -> Unit
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_aerialway_bicycle_summer)) { onAnswer(SUMMER) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_hairdresser_not_signed)) { onAnswer(NO_SIGN) }
        )
    )
}
