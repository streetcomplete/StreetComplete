package de.westnordost.streetcomplete.quests.traffic_signals_sound

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsSound(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters =
        "nodes with highway = crossing and crossing = traffic_signals and !traffic_signals:sound"
    override val commitMessage = "add traffic_signals:sound tag"
    override val icon = R.drawable.ic_quest_blind_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_sound_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("traffic_signals:sound", if (answer) "yes" else "no")
    }
}
