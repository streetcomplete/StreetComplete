package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.ktx.containsAny
import java.time.LocalDate

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

    override fun getTitle(tags: Map<String, String>) = when {
        hasProperName(tags)  -> R.string.quest_shop_type_title
        else            -> R.string.quest_shop_type_title_no_name
    }

    override fun createForm() = ShopTypeForm()

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand", "operator"))


    override fun applyAnswerTo(answer: ShopTypeAnswer, changes: StringMapChangesBuilder) {
        val otherCheckDateKeys = LAST_CHECK_DATE_KEYS.filterNot { it == SURVEY_MARK_KEY }
        for (otherCheckDateKey in otherCheckDateKeys) {
            changes.deleteIfExists(otherCheckDateKey)
        }
        when (answer) {
            is IsShopVacant -> {
                changes.addOrModify(SURVEY_MARK_KEY, LocalDate.now().toCheckDateString())
            }
            is ShopType -> {
                changes.deleteIfExists("disused:shop")
                if (!answer.tags.containsKey("shop")) {
                    changes.deleteIfExists("shop")
                }
                changes.deleteIfExists(SURVEY_MARK_KEY)
                for ((key, value) in answer.tags) {
                    changes.addOrModify(key, value)
                }

            }
        }
    }
}
