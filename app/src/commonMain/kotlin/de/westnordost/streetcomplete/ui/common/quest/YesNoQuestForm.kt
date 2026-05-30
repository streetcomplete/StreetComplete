package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun YesNoQuestForm(
    onAnswer: (QuestAnswer<Boolean>) -> Unit,
    title: String = stringResource(LocalQuestType.current!!.title),
) {
    QuestForm(
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(Answer(false)) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(Answer(true)) }
        ),
        onAnswer = onAnswer,
        title = title
    )
}
