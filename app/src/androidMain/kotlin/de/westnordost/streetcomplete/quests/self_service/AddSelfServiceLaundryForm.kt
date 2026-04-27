package de.westnordost.streetcomplete.quests.self_service

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddSelfServiceLaundryForm : AbstractOsmQuestForm<SelfServiceLaundry>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_optional)) { applyAnswer(OPTIONAL) },
                Answer(stringResource(Res.string.quest_hasFeature_only)) { applyAnswer(ONLY) }
            )
        )
    }
}
