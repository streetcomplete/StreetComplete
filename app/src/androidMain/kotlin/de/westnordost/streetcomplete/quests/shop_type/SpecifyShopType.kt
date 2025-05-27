package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.removePlaceRelatedTags

class SpecifyShopType : OsmFilterQuestType<ShopTypeAnswer>() {

    override val elementFilter = """
        nodes, ways with (
         shop ~ yes|hobby
         and !man_made
         and !historic
         and !military
         and !power
         and !tourism
         and !attraction
         and !amenity
         and !leisure
         and !aeroway
         and !railway
         and !craft
         and !healthcare
         and !office
        )
    """
    override val changesetComment = "Survey shop types"
    override val wikiLink = "Key:shop"
    override val icon = R.drawable.ic_quest_shop
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shop_type_title2

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = ShopTypeForm()

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.removeCheckDates()
        when (answer) {
            is IsShopVacant -> {
                val shopTag = tags["shop"]
                removePlaceRelatedTags(tags)
                tags["disused:shop"] = shopTag ?: "yes"
            }
            is ShopType -> {
                answer.feature.applyReplacePlaceTo(tags)
            }
        }
    }
}
