package de.westnordost.streetcomplete.quests.shop_type

import androidx.compose.runtime.Composable
import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.QuestAction
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.places.applyReplacePlaceTo
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.places.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.removeCheckDates
import de.westnordost.streetcomplete.osm.places.removePlaceRelatedTags
import de.westnordost.streetcomplete.resources.*
import de.westnordost.streetcomplete.util.getNameLabel

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
    override val icon = Res.drawable.quest_shop
    override val title = Res.string.quest_shop_type_title2
    override val achievements = listOf(CITIZEN)

    override fun getHighlightedElements(element: Element, mapData: MapDataWithGeometry) =
        mapData.asSequence().filter { it.isPlaceOrDisusedPlace() }

    @Composable
    override fun Form(on: (QuestAction<ShopTypeAnswer>) -> Unit, element: Element, geometry: ElementGeometry, countryInfo: CountryInfo) {
        ShopTypeQuestForm(on, element, countryInfo)
    }

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.removeCheckDates()
        when (answer) {
            is ShopTypeAnswer.IsShopVacant -> {
                val shopTag = tags["shop"]
                removePlaceRelatedTags(tags)
                tags["disused:shop"] = shopTag ?: "yes"
            }
            is ShopType -> {
                // if the shop has **some** name (that is displayed to the user), we just want to
                // update the shop, not replace it. The train of thought is:
                // 1. when the user is asked about what kind of shop <named thing> is, but doesn't
                //    see any shop by that name, he will just answer that it doesn't exist via
                //    Uh… -> Doesn't exist.
                // 2. When on the other hand he *does* see a shop by that name, it is quite clear
                //    that it is still the same shop, so we only update it, not replace it.
                // 3. On the other hand, if the place has no name, the user will also not be able
                //    to answer whether the place is now a different one than before, so we rather
                //    replace it. (#6675)
                val hasSomeName = getNameLabel(tags) != null
                if (hasSomeName) {
                    answer.feature.applyTo(tags)
                } else {
                    answer.feature.applyReplacePlaceTo(tags)
                }
            }
            is ShopTypeAnswer.LeaveNote -> { /* already handled by form */ }
        }
    }
}
