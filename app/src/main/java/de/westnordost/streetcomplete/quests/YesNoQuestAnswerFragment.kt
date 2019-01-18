package de.westnordost.streetcomplete.quests

class YesNoQuestAnswerFragment : AYesNoQuestAnswerFragment<Boolean>() {

    override fun onClick(answer: Boolean) { applyAnswer(answer) }
}
