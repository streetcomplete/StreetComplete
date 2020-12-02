package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment

class CheckExistenceForm : AYesNoQuestAnswerFragment<Unit>() {
    override fun onClick(answer: Boolean) {
        if (answer) applyAnswer(Unit)
        else deleteElement()
    }
}
