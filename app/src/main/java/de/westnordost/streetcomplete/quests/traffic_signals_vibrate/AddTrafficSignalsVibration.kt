package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddTrafficSignalsVibration(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<Boolean>(o) {

    override val tagFilters = """
        nodes with crossing = traffic_signals and highway ~ crossing|traffic_signals 
        and (
          !$VIBRATING_BUTTON
          or $VIBRATING_BUTTON = no and $VIBRATING_BUTTON older today -${r * 4} years
          or $VIBRATING_BUTTON older today -${r * 8} years
        )
    """

    override val commitMessage = "Add $VIBRATING_BUTTON tag"
    override val wikiLink = "Key:$VIBRATING_BUTTON"
    override val icon = R.drawable.ic_quest_blind_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_vibrate_title

    override fun createForm() = AddTrafficSignalsVibrationForm()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate(VIBRATING_BUTTON, answer.toYesNo())
    }
}

private const val VIBRATING_BUTTON = "traffic_signals:vibration"