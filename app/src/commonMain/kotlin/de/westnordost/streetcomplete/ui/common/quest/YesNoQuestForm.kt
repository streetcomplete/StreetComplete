package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_no
import de.westnordost.streetcomplete.resources.quest_generic_hasFeature_yes
import org.jetbrains.compose.resources.stringResource

@Composable
fun YesNoQuestForm(
    onAnswer: (Boolean) -> Unit,
    title: String = stringResource(LocalQuestType.current!!.title),
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(false) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(true) }
        ),
        title = title
    )
}
