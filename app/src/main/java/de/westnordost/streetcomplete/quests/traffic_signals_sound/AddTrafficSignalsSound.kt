package de.westnordost.streetcomplete.quests.traffic_signals_sound

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTrafficSignalsSound(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes with highway = crossing and crossing = traffic_signals
        and (
          !traffic_signals:sound
          or traffic_signals:sound = no and traffic_signals:sound older today -${r * 4} years
          or traffic_signals:sound older today -${r * 8} years
        )
    """

    override val commitMessage = "add traffic_signals:sound tag"
    override val wikiLink = "Tag:highway=traffic_signals"
    override val icon = R.drawable.ic_quest_blind_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_sound_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("traffic_signals:sound", if (answer) "yes" else "no")
    }
}
