package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.IS_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.meta.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.meta.KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED
import de.westnordost.streetcomplete.data.meta.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.data.meta.isShopExpressionFragment
import de.westnordost.streetcomplete.data.meta.removeCheckDates
import de.westnordost.streetcomplete.data.meta.updateCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN

class CheckShopType : OsmElementQuestType<ShopTypeAnswer> {

    private val disusedShopsFilter by lazy { """
        nodes, ways, relations with (
          shop = vacant
          or ${isShopExpressionFragment("disused")}
        ) and (
          older today -1 years
          or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -1 years" }}
        )
    """.toElementFilterExpression() }

    override val changesetComment = "Check if vacant shop is still vacant"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_check_shop

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_shop_vacant_type_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        disusedShopsFilter.matches(element)
        /* elements tagged like "shop=ice_cream + disused:amenity=bank" should not appear as quests.
         *  This is arguably a tagging mistake, but that mistake should not lead to all the tags of
         *  this element being cleared when the quest is answered */
        && !IS_SHOP_EXPRESSION.matches(element)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = ShopTypeForm()

    override fun applyAnswerTo(answer: ShopTypeAnswer, tags: Tags, timestampEdited: Long) {
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
