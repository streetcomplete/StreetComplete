package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.deleteCheckDates
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.containsAny

class SpecifyShopType : OsmFilterQuestType<ShopTypeAnswer>() {

    override val elementFilter = """
        nodes, ways, relations with (
             shop = yes
             and !man_made
             and !historic
             and !military
             and !power
             and !tourism
             and !attraction
             and !amenity
             and !craft
             and !tourism
             and !leisure
          )
    """
    override val commitMessage = "Specify shop type"
    override val wikiLink = "Key:shop"
    override val icon = R.drawable.ic_quest_check_shop
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = when {
        hasProperName(tags)  -> R.string.quest_shop_type_title
        else            -> R.string.quest_shop_type_title_no_name
    }

    override fun createForm() = ShopTypeForm()

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand", "operator"))


    override fun applyAnswerTo(answer: ShopTypeAnswer, changes: StringMapChangesBuilder) {
        changes.deleteCheckDates()
        when (answer) {
            is IsShopVacant -> {
                changes.deleteIfExists("shop")
                changes.addOrModify("disused:shop", "yes")
            }
            is ShopType -> {
                changes.deleteIfExists("disused:shop")
                if (!answer.tags.containsKey("shop")) {
                    changes.deleteIfExists("shop")
                }
                for ((key, value) in answer.tags) {
                    changes.addOrModify(key, value)
                }

            }
        }
    }
}
