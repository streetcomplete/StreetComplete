package de.westnordost.streetcomplete.quests.traffic_signals_sound

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsSound(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters =
        "nodes with highway = crossing and crossing = traffic_signals and !traffic_signals:sound"
    override val commitMessage = "add traffic_signals:sound tag"
    override val icon = R.drawable.ic_quest_blind_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_sound_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("traffic_signals:sound", yesno)
    }
}
