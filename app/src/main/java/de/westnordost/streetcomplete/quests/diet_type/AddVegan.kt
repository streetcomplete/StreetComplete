package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.VEG

class AddVegan : OsmFilterQuestType<DietAvailability>() {

    override val elementFilter = """
        nodes, ways with
        (
          amenity = ice_cream
          or diet:vegetarian ~ yes|only and
          (
            amenity ~ restaurant|cafe|fast_food
            or amenity = pub and food = yes
          )
        )
        and name and (
          !diet:vegan
          or diet:vegan != only and diet:vegan older today -2 years
        )
    """
    override val commitMessage = "Add vegan diet type"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.ic_quest_restaurant_vegan
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override val questTypeAchievements = listOf(VEG, CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_dietType_vegan_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegan)

    override fun applyAnswerTo(answer: DietAvailability, changes: StringMapChangesBuilder) {
        changes.updateWithCheckDate("diet:vegan", answer.osmValue)
    }
}
