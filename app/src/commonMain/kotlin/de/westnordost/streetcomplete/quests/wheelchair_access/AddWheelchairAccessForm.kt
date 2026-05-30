package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAnswer
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddWheelchairAccessForm(
    onAnswer: (QuestAnswer<WheelchairAccess>) -> Unit,
) {
    QuestForm(
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { onAnswer(Answer(NO)) },
            AnswerItem(stringResource(Res.string.quest_wheelchairAccess_limited)) { onAnswer(Answer(LIMITED)) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { onAnswer(Answer(YES)) },
        ),
        onAnswer = onAnswer,
    )
}
