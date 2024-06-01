package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isDisusedPlace
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.replacePlace
import de.westnordost.streetcomplete.osm.updateCheckDate

class CheckShopType : OsmElementQuestType<ShopTypeAnswer> {

    private val filter by lazy { """
        nodes, ways with
          !man_made
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
          and (!office or office = vacant)
          and (!shop or shop = vacant)
          and (
            older today -1 years
            or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -1 years" }}
          )
    """.toElementFilterExpression() }

    override val changesetComment = "Survey if vacant shops are still vacant"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_check_shop
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shop_vacant_type_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        element.isDisusedPlace() &&
        filter.matches(element) &&
        /* elements tagged like "shop=ice_cream + disused:amenity=bank" should not appear as quests.
         *  This is arguably a tagging mistake, but that mistake should not lead to all the tags of
         *  this element being cleared when the quest is answered */
        !element.isPlace()

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = ShopTypeForm()

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        when (answer) {
            is IsShopVacant -> {
                tags.updateCheckDate()
            }
            is ShopType -> {
                tags.replacePlace(answer.tags)
            }
        }
    }
}
