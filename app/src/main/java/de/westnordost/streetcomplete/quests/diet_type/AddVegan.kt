package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddVegan : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with 
        (
          amenity ~ restaurant|cafe|fast_food and diet:vegetarian ~ yes|only 
          or amenity = ice_cream
        )
        and name and (
          !diet:vegan 
          or diet:vegan != only and diet:vegan older today -2 years
        )
    """
    override val commitMessage = "Add vegan diet type"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegan_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan)

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("diet:vegan", answer)
    }
}
