package de.westnordost.streetcomplete.quests.traffic_signals_button

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsButton(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters =
        "nodes with highway = crossing and crossing = traffic_signals and !button_operated"
    override val commitMessage = "add whether traffic signals have a button for pedestrians"
    override val icon = R.drawable.ic_quest_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_button_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("button_operated", if (answer) "yes" else "no")
    }
}
