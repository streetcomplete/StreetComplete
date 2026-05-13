package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddWheelchairAccessForm(
    onAnswer: (WheelchairAccess) -> Unit,
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_wheelchairAccess_limited)) { onAnswer(LIMITED) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) },
        )
    )
}
