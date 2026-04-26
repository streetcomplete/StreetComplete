package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class CheckShopExistenceForm : AbstractOsmQuestForm<Unit>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { replacePlace() },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(Unit) }
            )
        )
    }
}
