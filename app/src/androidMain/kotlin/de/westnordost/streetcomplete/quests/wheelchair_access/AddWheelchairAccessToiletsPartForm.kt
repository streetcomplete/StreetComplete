package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddWheelchairAccessToiletsPartForm(
    onAnswer: (WheelchairAccessToiletsPartAnswer) -> Unit
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                onAnswer(WheelchairAccessToiletsPart(NO))
            },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                onAnswer(WheelchairAccessToiletsPart(YES))
            },
            Answer(stringResource(Res.string.quest_wheelchairAccess_limited)) {
                onAnswer(WheelchairAccessToiletsPart(LIMITED))
            },
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_wheelchairAccessPat_noToilet)) {
                onAnswer(WheelchairAccessToiletsPartAnswer.NoToilet)
            }
        )
    )
}
