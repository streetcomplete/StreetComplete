package de.westnordost.streetcomplete.quests.show_poi

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.updateCheckDate
import de.westnordost.streetcomplete.quests.shop_type.IsShopVacant
import de.westnordost.streetcomplete.quests.shop_type.ShopType
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeAnswer
import de.westnordost.streetcomplete.quests.shop_type.ShopTypeForm

class ShowVacant : OsmFilterQuestType<ShopTypeAnswer>() {
    override val elementFilter = """
        nodes, ways, relations with
        shop = vacant
        or disused:shop
        or disused:amenity
        or disused:office
    """
    override val changesetComment = "Adjust vacant places"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_poi_vacant
    override val dotColor = "grey"
    override val defaultDisabledMessage = R.string.default_disabled_msg_poi_vacant

    override fun getTitle(tags: Map<String, String>) =
        R.string.quest_poi_vacant_title

    override fun createForm() = ShopTypeForm()

    override fun getTitleArgs(tags: Map<String, String>): Array<String> {
        return arrayOf(tags.entries.toString())
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is IsShopVacant -> {
                tags.updateCheckDate()
            }
            is ShopType -> {
                tags.removeCheckDates()

                for (key in tags.keys) {
                    if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED.any { it.matches(key) }) {
                        tags.remove(key)
                    }
                }

                for ((key, value) in answer.tags) {
                    tags[key] = value
                }
            }
        }
    }
}
