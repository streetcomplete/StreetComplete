package de.westnordost.streetcomplete.quests.recycling_glass

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.recycling_glass.RecyclingGlass.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun DetermineRecyclingGlassForm(
    onAnswer: (RecyclingGlass) -> Unit,
) {
    QuestForm(
        answers = Answers(
            Answer(stringResource(Res.string.quest_recycling_type_any_glass)) { onAnswer(ANY) },
            Answer(stringResource(Res.string.quest_recycling_type_glass_bottles_short)) { onAnswer(BOTTLES) }
        )
    )
}
