package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquest.OsmFilterQuestType

class AddKosher : OsmFilterQuestType<String>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity ~ restaurant|cafe|fast_food
          or amenity = ice_cream
          or shop = butcher
        )
        and name and (
          !diet:kosher
          or diet:kosher != only and diet:kosher older today -4 years
        )
    """
    override val commitMessage = "Add kosher status"
    override val wikiLink = "Key:diet:kosher"
    override val icon = R.drawable.ic_quest_kosher
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside_usefullness_warning

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_kosher_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_kosher)

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("diet:vegan", answer)
    }
}
