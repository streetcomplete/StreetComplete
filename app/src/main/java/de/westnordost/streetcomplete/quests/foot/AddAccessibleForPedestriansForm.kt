package de.westnordost.streetcomplete.quests.foot

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment

class AddAccessibleForPedestriansForm : AYesNoQuestAnswerFragment<Boolean>() {

    override val contentLayoutResId = R.layout.quest_accessible_for_pedestrians_explanation

    override fun onClick(answer: Boolean) { applyAnswer(answer) }
}
