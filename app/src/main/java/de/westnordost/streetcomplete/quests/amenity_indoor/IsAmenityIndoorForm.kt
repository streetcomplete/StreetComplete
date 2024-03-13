package de.westnordost.streetcomplete.quests.amenity_indoor

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class IsAmenityIndoorForm : AbstractOsmQuestForm<IsAmenityIndoorAnswer>() {
    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(IsAmenityIndoorAnswer.OUTDOOR) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(IsAmenityIndoorAnswer.INDOOR) }
    )

    override val otherAnswers = listOf(
        AnswerItem(R.string.quest_isAmenityIndoor_outside_covered) { applyAnswer(IsAmenityIndoorAnswer.COVERED) }
    )
}
