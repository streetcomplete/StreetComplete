package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowTrafficStuffAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (element.tags["traffic_calming"] == null && element.tags["crossing"] != null)
            buttonPanelAnswers.add(AnswerItem(R.string.quest_traffic_stuff_raised) { applyAnswer(true) })
    }

}
