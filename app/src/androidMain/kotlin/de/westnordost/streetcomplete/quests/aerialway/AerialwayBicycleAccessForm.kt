package de.westnordost.streetcomplete.quests.aerialway

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem
import de.westnordost.streetcomplete.quests.amenity_indoor.IsAmenityIndoorAnswer

class AerialwayBicycleAccessForm : AbstractOsmQuestForm<AerialwayBicycleAccessAnswer>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(AerialwayBicycleAccessAnswer.NO) },
        AnswerItem(R.string.quest_aerialway_bicycle_summer) { applyAnswer(AerialwayBicycleAccessAnswer.SUMMER) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(AerialwayBicycleAccessAnswer.YES) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_hairdresser_not_signed) { applyAnswer(AerialwayBicycleAccessAnswer.NO_SIGN) }
    )
}
