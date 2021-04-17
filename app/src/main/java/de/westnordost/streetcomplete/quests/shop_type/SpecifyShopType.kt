package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ktx.containsAny

class SpecifyShopType : CheckShopType() {

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
             and !leisure
          )
    """
    override val commitMessage = "Specify shop type"
    override val wikiLink = "Key:shop"
    override val icon = R.drawable.ic_quest_check_shop // TODO nice icon would be nice (reuse graphic from building quest?)

    override fun getTitle(tags: Map<String, String>) = when {
        hasProperName(tags)  -> R.string.quest_shop_type_title
        else            -> R.string.quest_shop_type_title_no_name
    }

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(listOf("name", "brand", "operator"))

}
