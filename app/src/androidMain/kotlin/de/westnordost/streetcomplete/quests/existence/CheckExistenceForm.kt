package de.westnordost.streetcomplete.quests.existence

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun CheckExistenceForm(
    onAnswer: (Unit) -> Unit,
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { deletePoiNode() },
            Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(Unit) }
        )
    )
}
