package de.westnordost.streetcomplete.quests.traffic_signals_button

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsButton @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "nodes with highway=crossing and crossing=traffic_signals and !button_operated"

    override val commitMessage: String
        get() = "add whether traffic signals have a button for pedestrians"
    override val icon: Int
        get() = R.drawable.ic_quest_traffic_lights

    override fun createForm(): AbstractQuestAnswerFragment {
        return YesNoQuestAnswerFragment()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
            changes.add("button_operated", "yes")
        } else {
            changes.add("button_operated", "no")
        }
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_traffic_signals_button_title
    }
}
