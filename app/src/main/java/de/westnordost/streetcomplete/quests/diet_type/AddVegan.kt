package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataAndGeometryDao

class AddVegan(o: OverpassMapDataAndGeometryDao) : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways with 
        (
          amenity ~ restaurant|cafe|fast_food and diet:vegetarian ~ yes|only 
          or amenity = ice_cream
        )
        and name and !diet:vegan
    """
    override val commitMessage = "Add vegan diet type"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegan_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan)

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("diet:vegan", answer)
    }
}
