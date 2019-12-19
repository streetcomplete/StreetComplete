package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddSport(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<List<String>>(o) {

    private val ambiguousSportValues = listOf(
        "team_handball", // -> not really ambiguous but same as handball
        "hockey", // -> ice_hockey or field_hockey
        "skating", // -> ice_skating or roller_skating
        "football" // -> american_football, soccer or other *_football
    )

    override val tagFilters = """
        nodes, ways with leisure = pitch and
        (!sport or sport ~ ${ambiguousSportValues.joinToString("|")} )
        and (access !~ private|no)
    """
    override val commitMessage = "Add pitches sport"
    override val icon = R.drawable.ic_quest_sport

    override fun getTitle(tags: Map<String, String>) = R.string.quest_sport_title

    override fun createForm() = AddSportForm()

    override fun applyAnswerTo(answer: List<String>, changes: StringMapChangesBuilder) {
        val previousValue = changes.getPreviousValue("sport")
        val values = answer.joinToString(";")
        // only modify the previous values in case of these ~deprecated ones, otherwise assume
        // always that the tag has not been set yet (will drop the solution if it has been set
        // in the meantime by other people) (#291)
        if (ambiguousSportValues.contains(previousValue)) {
            changes.modify("sport", values)
        } else {
            changes.add("sport", values)
        }
    }
}
