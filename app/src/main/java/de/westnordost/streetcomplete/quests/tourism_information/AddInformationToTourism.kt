package de.westnordost.streetcomplete.quests.tourism_information

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder

class AddInformationToTourism : OsmFilterQuestType<TourismInformation>() {

    override val elementFilter = "nodes, ways, relations with tourism = information and !information"
    override val commitMessage = "Add information type to tourist information"
    override val wikiLink = "Tag:tourism=information"
    override val icon = R.drawable.ic_quest_information

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsKey("name"))
            R.string.quest_tourism_information_name_title
        else
            R.string.quest_tourism_information_title

    override fun createForm() = AddInformationForm()

    override fun applyAnswerTo(answer: TourismInformation, changes: StringMapChangesBuilder) {
        changes.add("information", answer.osmValue)
    }
}
