package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AYesNoQuestAnswerFragment

class AddTrafficSignalsVibrationForm : AYesNoQuestAnswerFragment<Boolean>() {

    override val contentLayoutResId = R.layout.quest_traffic_lights_vibration

    override fun onClick(answer: Boolean) { applyAnswer(answer) }

}