package de.westnordost.streetcomplete.quests.traffic_signals_button

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddTrafficSignalsButton : OsmFilterQuestType<Boolean>() {

    override val elementFilter = """
        nodes with crossing = traffic_signals and highway ~ crossing|traffic_signals 
        and !button_operated
        """
    override val commitMessage = "Add whether traffic signals have a button for pedestrians"
    override val wikiLink = "Tag:highway=traffic_signals"
    override val icon = R.drawable.ic_quest_traffic_lights

    override fun getTitle(tags: Map<String, String>) = R.string.quest_traffic_signals_button_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("button_operated", answer.toYesNo())
    }
}
