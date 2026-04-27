package de.westnordost.streetcomplete.quests.ferry

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.ferry.FerryHgvAccess.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddFerryAccessHgvForm : AbstractOsmQuestForm<FerryHgvAccess>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(YES) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_generic_answer_noSign)) { applyAnswer(NOT_SIGNED) }
            )
        )
    }
}
