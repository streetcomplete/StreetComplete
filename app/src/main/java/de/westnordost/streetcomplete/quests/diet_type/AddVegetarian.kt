package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.OverpassMapDataAndGeometryApi
import de.westnordost.streetcomplete.settings.ResurveyIntervalsStore

class AddVegetarian(o: OverpassMapDataAndGeometryApi, r: ResurveyIntervalsStore)
    : SimpleOverpassQuestType<String>(o) {

    override val tagFilters = """
        nodes, ways with amenity ~ restaurant|cafe|fast_food
        and name and (
          !diet:vegetarian 
          or diet:vegetarian != only and diet:vegetarian older today -${r * 2} years
        )
    """

    override val commitMessage = "Add vegetarian diet type"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.ic_quest_restaurant_vegetarian
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegetarian_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegetarian)

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("diet:vegetarian", answer)
    }
}
