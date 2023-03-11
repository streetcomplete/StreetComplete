package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AnswerItem

class AddWheelchairAccessToiletsForm : WheelchairAccessForm() {
    override val contentLayoutResId = R.layout.quest_wheelchair_toilets_explanation

    override val otherAnswers get() = listOf(
        AnswerItem(R.string.quest_wheelchairAccessPat_noToilet) { applyAnswer(NoToilet) }
    )
}
