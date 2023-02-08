package de.westnordost.streetcomplete.quests.traffic_signals_sound

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class AddTrafficSignalsSoundForm : AbstractOsmQuestForm<Boolean>() {

    override val contentLayoutResId = R.layout.quest_traffic_lights_sound

    override val buttonPanelAnswers = listOf(
        AnswerItem(R.string.quest_generic_hasFeature_no) { applyAnswer(false) },
        AnswerItem(R.string.quest_generic_hasFeature_yes) { applyAnswer(true) }
    )
}
