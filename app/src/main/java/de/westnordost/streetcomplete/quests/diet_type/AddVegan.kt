package de.westnordost.streetcomplete.quests.diet_type

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddVegan(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters = """
        nodes, ways with amenity ~ restaurant|cafe|fast_food
        and name and diet:vegetarian ~ yes|only and !diet:vegan
    """
    override val commitMessage = "Add vegan diet type"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegan_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan)

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        changes.add("diet:vegan", answer.getString(AddDietTypeForm.OSM_VALUE)!!)
    }
}
