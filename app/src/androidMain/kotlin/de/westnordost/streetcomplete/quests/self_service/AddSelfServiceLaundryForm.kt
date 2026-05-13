package de.westnordost.streetcomplete.quests.self_service

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.quests.self_service.SelfServiceLaundry.*
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddSelfServiceLaundryForm(
    onAnswer: (SelfServiceLaundry) -> Unit
) {
    QuestForm(
        answers = listOf(
            Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(NO) },
            Answer(stringResource(Res.string.quest_generic_hasFeature_optional)) { onAnswer(OPTIONAL) },
            Answer(stringResource(Res.string.quest_hasFeature_only)) { onAnswer(ONLY) }
        )
    )
}
