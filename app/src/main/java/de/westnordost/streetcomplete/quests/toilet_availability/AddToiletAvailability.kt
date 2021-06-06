package de.westnordost.streetcomplete.quests.toilet_availability

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.ktx.toYesNo
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment

class AddToiletAvailability : OsmFilterQuestType<Boolean>() {

    // only for malls, big stores and rest areas because users should not need to go inside a non-public
    // place to solve the quest. (Considering malls and department stores public enough)
    override val elementFilter = """
        nodes, ways with
        (
          (shop ~ mall|department_store and name)
          or highway ~ services|rest_area
        )
        and !toilets
    """
    override val commitMessage = "Add toilet availability"
    override val wikiLink = "Key:toilets"
    override val icon = R.drawable.ic_quest_toilets

    override fun getTitle(tags: Map<String, String>) =
        if (tags["highway"] == "rest_area" || tags["highway"] == "services")
            R.string.quest_toiletAvailability_rest_area_title
        else
            R.string.quest_toiletAvailability_name_title

    override fun createForm() = YesNoQuestAnswerFragment()

    override fun applyAnswerTo(answer: Boolean, changes: StringMapChangesBuilder) {
        changes.add("toilets", answer.toYesNo())
    }
}
