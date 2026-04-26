package de.westnordost.streetcomplete.quests.aerialway

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.ui.common.quest.Answer
import de.westnordost.streetcomplete.quests.aerialway.AerialwayBicycleAccessAnswer.*
import de.westnordost.streetcomplete.quests.amenity_indoor.IsAmenityIndoorAnswer
import de.westnordost.streetcomplete.ui.common.quest.Answers
import de.westnordost.streetcomplete.ui.common.quest.QuestForm
import org.jetbrains.compose.resources.stringResource

class AerialwayBicycleAccessForm : AbstractOsmQuestForm<AerialwayBicycleAccessAnswer>() {

    @Composable
    override fun Content() {
        QuestForm(
            answers = Answers(
                Answer(stringResource(Res.string.quest_generic_hasFeature_no)) { applyAnswer(NO) },
                Answer(stringResource(Res.string.quest_aerialway_bicycle_summer)) { applyAnswer(SUMMER) },
                Answer(stringResource(Res.string.quest_generic_hasFeature_yes)) { applyAnswer(YES) }
            ),
            otherAnswers = listOf(
                Answer(stringResource(Res.string.quest_hairdresser_not_signed)) { applyAnswer(NO_SIGN) }
            )
        )
    }
}
