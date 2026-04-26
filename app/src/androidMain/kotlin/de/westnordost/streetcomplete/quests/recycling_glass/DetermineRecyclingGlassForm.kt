package de.westnordost.streetcomplete.quests.recycling_glass

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class DetermineRecyclingGlassForm : AbstractOsmQuestForm<RecyclingGlass>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_recycling_type_any_glass)) { applyAnswer(ANY) },
                Answer(stringResource(Res.string.quest_recycling_type_glass_bottles_short)) { applyAnswer(BOTTLES) }
            )
        )
    }
}
