package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowRecyclingAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (element.tags["amenity"] == "waste_basket")
            buttonPanelAnswers.add(AnswerItem(R.string.quest_recycling_excrement_bag_dispenser) { applyAnswer(true) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycling = element.tags.mapNotNull {
            if (it.value == "yes" && it.key.startsWith("recycling:"))
                it.key.substringAfter("recycling:")
            else null
        }.sorted().joinToString(", ")
        if (recycling.isNotEmpty())
            setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " $recycling")
    }

}
