package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.osm.osmquests.Answer
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.ui.common.quest.AnswerItem
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

@Composable
fun AddWheelchairAccessForm(
    on: (QuestAction<WheelchairAccess>) -> Unit,
) {
    QuestForm(
        on = on,
        answers = listOf(
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_no)) { on(Answer(NO)) },
            AnswerItem(stringResource(Res.string.quest_wheelchairAccess_limited)) { on(Answer(LIMITED)) },
            AnswerItem(stringResource(Res.string.quest_generic_hasFeature_yes)) { on(Answer(YES)) },
        ),
    )
}
