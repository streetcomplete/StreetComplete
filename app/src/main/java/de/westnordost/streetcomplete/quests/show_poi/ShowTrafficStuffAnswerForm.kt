package de.westnordost.streetcomplete.quests.show_poi

import android.os.Bundle
import android.view.View
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.quests.AbstractOsmQuestForm
import de.westnordost.streetcomplete.quests.AnswerItem

class ShowTrafficStuffAnswerForm : AbstractOsmQuestForm<Boolean>() {

    override val buttonPanelAnswers = mutableListOf<AnswerItem>()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        if (element.tags["traffic_calming"] == null && element.tags["crossing"] != null)
            buttonPanelAnswers.add(AnswerItem(R.string.quest_traffic_stuff_raised) { applyAnswer(true) })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if ((!element.tags["crossing"].isNullOrBlank() && !element.tags["traffic_calming"].isNullOrBlank())
            || element.tags["type"] == "restriction"
            || element.tags["highway"] == "elevator") {
            setTitle(resources.getString((questType as OsmElementQuestType<*>).getTitle(element.tags)) + " ${element.tags.entries}")
        }
    }

}
