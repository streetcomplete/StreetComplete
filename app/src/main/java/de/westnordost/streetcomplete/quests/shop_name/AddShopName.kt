package de.westnordost.streetcomplete.quests.shop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.quests.AbstractQuestAnswerFragment

class AddShopName(o: OverpassMapDataDao) : SimpleOverpassQuestType<ShopNameAnswer>(o) {
    override val tagFilters = """
        nodes, ways with ((amenity ~ post_office|pharmacy|bank|atm|bureau_de_change) or (shop)) and (!name and noname != yes)
    """
    override val commitMessage = "add shop name"
    override val icon = R.drawable.ic_quest_shop_name

    // TODO - add the correct name for the shop type (aka integrate with osmfeatures lib)
    override fun getTitle(tags: Map<String, String>) = R.string.quest_shopName_title

    override fun applyAnswerTo(answer: ShopNameAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is NoShopNameSign -> changes.add("noname", "yes")
            is ShopName -> changes.add("name", answer.name)
        }
    }

    override fun createForm() = AddShopNameForm()
}
