package de.westnordost.streetcomplete.quests.traffic_signals_sound

import android.os.Bundle

import javax.inject.Inject

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsSound @Inject constructor(overpassServer: OverpassMapDataDao) :
    SimpleOverpassQuestType(overpassServer) {

    override val tagFilters: String
        get() = "nodes with highway=crossing and crossing=traffic_signals and !traffic_signals:sound"

    override val commitMessage: String
        get() = "add traffic_signals:sound tag"
    override val icon: Int
        get() = R.drawable.ic_quest_blind_traffic_lights

    override fun createForm(): AbstractQuestAnswerFragment {
        return YesNoQuestAnswerFragment()
    }

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) {
            changes.add("traffic_signals:sound", "yes")
        } else {
            changes.add("traffic_signals:sound", "no")
        }
    }

    override fun getTitle(tags: Map<String, String>): Int {
        return R.string.quest_traffic_signals_sound_title
    }
}
