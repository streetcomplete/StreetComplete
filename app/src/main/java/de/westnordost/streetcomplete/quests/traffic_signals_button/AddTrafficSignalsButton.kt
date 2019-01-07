package de.westnordost.streetcomplete.quests.traffic_signals_button

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsButton(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters =
        "nodes with highway = crossing and crossing = traffic_signals and !button_operated"
    override val commitMessage = "add whether traffic signals have a button for pedestrians"
    override val icon = R.drawable.ic_quest_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_button_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("button_operated", yesno)
    }
}
