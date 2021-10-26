package de.westnordost.streetcomplete.quests.diet_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.prepareDietFilter
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.VEG

class AddVegetarian : OsmFilterQuestType<DietAvailabilityAnswer>() {

    override val elementFilter = prepareDietFilter("vegetarian")
    override val commitMessage = "Add vegetarian diet type"
    override val wikiLink = "Key:diet"
    override val icon = R.drawable.ic_quest_restaurant_vegetarian
    override val isReplaceShopEnabled = true
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override val questTypeAchievements = listOf(VEG, CITIZEN)

    override fun getTitle(tags: Map<String, String>) =
        if (tags.containsKey("shop"))
            R.string.quest_dietType_vegetarian_shop_name_title
        else
            R.string.quest_dietType_vegetarian_name_title

    override fun createForm() = AddDietTypeForm.create(R.string.quest_dietType_explanation_vegetarian)

    override fun applyAnswerTo(answer: DietAvailabilityAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is DietAvailability -> {
                changes.updateWithCheckDate("diet:vegetarian", answer.osmValue)
                if (answer.osmValue == "no") {
                    changes.deleteIfExists("diet:vegan")
                }
            }
            NoFood -> changes.addOrModify("food", "no")
        }
    }
}
