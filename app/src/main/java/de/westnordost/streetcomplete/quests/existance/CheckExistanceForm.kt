package de.westnordost.streetcomplete.quests.existance

import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment

class CheckExistanceForm : AYesNoQuestAnswerFragment<Unit>() {
    override fun onClick(answer: Boolean) {
        if (answer) applyAnswer(Unit)
        else deleteElement()
    }
}
