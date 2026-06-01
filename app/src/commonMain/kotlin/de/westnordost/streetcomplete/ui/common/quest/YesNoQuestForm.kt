package de.westnordost.streetcomplete.ui.common.quest

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun YesNoQuestForm(
    on: (QuestAction<Boolean>) -> Unit,
    title: String = stringResource(LocalQuestType.current!!.title),
) {
    QuestForm(
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(false)) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(true)) }
        ),
        on = on,
        title = title
    )
}
