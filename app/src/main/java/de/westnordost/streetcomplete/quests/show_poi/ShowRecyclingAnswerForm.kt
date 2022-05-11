package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowRecyclingAnswerForm : AbstractQuestAnswerFragment<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (osmElement!!.tags["amenity"] == "waste_basket")
            buttonPanelAnswers.add(AnswerItem(R.string.quest_recycling_excrement_bag_dispenser) { applyAnswer(true) })
    }

}
