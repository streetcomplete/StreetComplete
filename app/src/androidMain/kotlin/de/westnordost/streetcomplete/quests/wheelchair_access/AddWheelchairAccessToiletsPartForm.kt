package de.westnordost.streetcomplete.quests.wheelchair_access

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.wheelchair_access.WheelchairAccess.*
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AddWheelchairAccessToiletsPartForm : AbstractOsmQuestForm<WheelchairAccessToiletsPartAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) {
                    applyAnswer(WheelchairAccessToiletsPart(NO))
                                                                               },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) {
                    applyAnswer(WheelchairAccessToiletsPart(YES))
                },
                Answer(stringResource(Res.string.quest_wheelchairAccess_limited)) {
                    applyAnswer(WheelchairAccessToiletsPart(LIMITED))
                },
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_wheelchairAccessPat_noToilet)) { applyAnswer(NoToilet) }
            )
        )
    }
}
