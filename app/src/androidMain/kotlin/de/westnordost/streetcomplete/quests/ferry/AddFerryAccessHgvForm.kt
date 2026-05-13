package de.westnordost.streetcomplete.quests.ferry

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.ferry.FerryHgvAccess.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddFerryAccessHgvForm(
    onAnswer: (FerryHgvAccess) -> Unit,
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(YES) }
        ),
        otherAnswers = listOf(
            Answer(stringResource(Res.string.quest_generic_answer_noSign)) { onAnswer(NOT_SIGNED) }
        )
    )
}
