package de.westnordost.streetcomplete.quests.toilet_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletAvailability(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<Boolean>(o) {

    // only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    override val tagFilters = """
        nodes, ways with
        (
          (shop ~ mall|department_store and name)
          or highway ~ services|rest_area
        )
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

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("toilets", if (answer) "yes" else "no")
    }
}
