package de.westnordost.streetcomplete.quests.shop_type

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.isPlace
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.updateCheckDate

class CheckShopExistence(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<Unit> {
    // opening hours quest acts as a de facto checker of shop existence, but some people disabled it.
    // separate from CheckExistence as very old shop with opening hours should show
    // opening hours resurvey quest rather than this one (which would cause edit date to be changed
    // and silence all resurvey quests)
    private val filter by lazy { ("""
        nodes, ways with
          !man_made
          and !historic
          and !military
          and !power
          and !attraction
          and !aeroway
          and !railway
          and (
            older today -2 years
            or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -2 years" }}
          )
          and (name or brand or noname = yes or name:signed = no)
    """).toElementFilterExpression() }

    override val changesetComment = "Survey if places (shops and other shop-like) still exist"
    override val wikiLink = "Key:disused:"
    override val icon = R.drawable.ic_quest_check_shop
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_existence_title2

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) &&
        element.isPlace() &&
        hasName(element)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = CheckShopExistenceForm()

    override fun applyAnswerTo(answer: Unit, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
        tags.updateCheckDate()
    }

    private fun hasName(element: Element) = hasProperName(element.tags) || hasFeatureName(element)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.containsKey("name") || tags.containsKey("brand")

    private fun hasFeatureName(element: Element) = getFeature(element)?.name != null
}
