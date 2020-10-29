package de.westnordost.streetcomplete.quests.tourism_information

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddInformationToTourism : OsmFilterQuestType<String>() {

    override val elementFilter = "nodes, ways, relations with tourism = information and !information"
    override val commitMessage = "Add information type to tourist information"
    override val wikiLink = "Tag:tourism=information"
    override val icon = R.drawable.ic_quest_information

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_tourism_information_name_title
        else
            R.string.quest_tourism_information_title
    }

    override fun createForm() = AddInformationForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("information", answer)
    }
}
