package de.westnordost.streetcomplete.quests.bollard_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType

class AddBollardType : OsmFilterQuestType<BollardType>() {

    override val elementFilter = """
        nodes with
            barrier = bollard
            and !bollard
    """
    override val commitMessage = "Add bollard type"
    override val wikiLink = "Key:bollard"
    override val icon = R.drawable.ic_quest_no_cars
    override val isDeleteElementEnabled = true

    override fun getTitle(tags: Map<String, String>) = R.string.quest_bollard_type_title

    override fun createForm() = AddBollardTypeForm()

    override fun applyAnswerTo(answer: BollardType, changes: StringMapChangesBuilder) {
        changes.add("bollard", answer.osmValue)
    }
}
