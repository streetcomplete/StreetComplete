package de.westnordost.streetcomplete.quests.amenity_indoor

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.amenity_indoor.IsAmenityIndoorAnswer.*
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class IsAmenityIndoorForm : AbstractOsmQuestForm<IsAmenityIndoorAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(OUTDOOR) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(INDOOR) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_isAmenityIndoor_outside_covered)) { applyAnswer(COVERED) }
            )
        )
    }
}
