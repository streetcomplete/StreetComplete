package de.westnordost.streetcomplete.quests.traffic_signals_vibrate

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo

class AddTrafficSignalsVibration : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with crossing = traffic_signals and highway ~ crossing|traffic_signals 
        and (
          !$VIBRATING_BUTTON
          or $VIBRATING_BUTTON = no and $VIBRATING_BUTTON older today -4 years
          or $VIBRATING_BUTTON older today -8 years
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