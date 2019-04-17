package de.westnordost.streetcomplete.quests.shop_name

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddShopName(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o) {
    override val tagFilters = """
        nodes, ways with ((amenity ~ post_office|pharmacy|bank|atm|bureau_de_change) or (shop)) and (!name and noname != yes)
    """
    override val commitMessage = "add shop name"
    override val icon = R.drawable.ic_quest_shop_name

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shopName_title

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("name", answer)
    }
}
