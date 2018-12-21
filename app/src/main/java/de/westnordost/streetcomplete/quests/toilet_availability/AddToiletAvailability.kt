package de.westnordost.streetcomplete.quests.toilet_availability

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletAvailability(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    // only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    override val tagFilters = """
        nodes, ways with
        ( (shop ~ mall|department_store and name) or (highway ~ services|rest_area) )
        and !toilets
    """
    override val commitMessage = "Add toilet availability"
    override val icon = R.drawable.ic_quest_toilets

    override fun getTitle(tags: Map<String, String>) =
        if (tags["highway"] == "rest_area" || tags["highway"] == "services")
            R.string.quest_toiletAvailability_rest_area_title
        else
            R.string.quest_toiletAvailability_name_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        val yesno = if (answer.getBoolean(YesNoQuestAnswerFragment.ANSWER)) "yes" else "no"
        changes.add("toilets", yesno)
    }
}
