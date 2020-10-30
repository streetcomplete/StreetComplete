package de.westnordost.streetcomplete.quests.religion

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddReligionToPlaceOfWorship : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways, relations with 
        (
            amenity = place_of_worship
            or
            amenity = monastery
        )
        and !religion
    """
    override val commitMessage = "Add religion for place of worship"
    override val wikiLink = "Key:religion"
    override val icon = R.drawable.ic_quest_religion

    override fun getTitle(tags: Map<String, String>): Int {
        val hasName = tags.containsKey("name")
        return if (hasName)
            R.string.quest_religion_for_place_of_worship_name_title
        else
            R.string.quest_religion_for_place_of_worship_title
    }

    override fun createForm() = AddReligionForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("religion", answer)
    }
}
