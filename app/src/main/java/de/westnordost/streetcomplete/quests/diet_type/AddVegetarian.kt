package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddVegetarian(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways with amenity ~ restaurant|cafe|fast_food
        and name and !diet:vegetarian
    """
    override val commitMessage = "Add vegetarian diet type"
    override val icon = R.drawable.ic_quest_restaurant_vegetarian
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegetarian_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegetarian)

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("diet:vegetarian", answer)
    }
}
