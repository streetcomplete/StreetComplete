package de.westnordost.streetcomplete.quests.existence

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.data.meta.updateCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.OUTDOORS
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import de.westnordost.streetcomplete.quests.getNameOrBrandOrOperatorOrRef
import java.util.concurrent.FutureTask

class CheckExistence(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<Unit> {

    private val nodesFilter by lazy { """
        nodes with ((
          (
            amenity = atm
            or amenity = telephone
            or amenity = vending_machine and vending !~ fuel|parking_tickets|public_transport_tickets
            or amenity = public_bookcase
          )
          and (${lastChecked(2.0)})
        ) or (
          (
            amenity = clock
            or amenity = post_box
            or leisure = picnic_table
            or amenity = bbq
            or leisure = firepit
            or amenity = vending_machine and vending ~ parking_tickets|public_transport_tickets
            or amenity = ticket_validator
            or tourism = information and information ~ board|terminal|map
            or advertising ~ column|board|poster_box
            or (highway = emergency_access_point or emergency = access_point) and ref
            or emergency ~ life_ring|phone
            or (emergency = defibrillator and indoor = no)
            or (
              man_made = surveillance and surveillance:type = camera and surveillance ~ outdoor|public
              and !highway
            )
          )
          and (${lastChecked(4.0)})
        ) or (
          (
            amenity = bench
            or amenity = lounger
            or amenity = waste_basket
            or traffic_calming ~ bump|hump|island|cushion|choker|rumble_strip|chicane|dip
            or traffic_calming = table and !highway and !crossing
            or amenity = recycling and recycling_type = container
            or amenity = toilets
            or amenity = drinking_water
          )
          and (${lastChecked(6.0)})
        ) or (
          (
            amenity ~ bicycle_parking|motorcycle_parking
          )
          and (${lastChecked(12.0)})
        )) and access !~ no|private and (!seasonal or seasonal = no)
    """.toElementFilterExpression() }
    // traffic_calming = table is often used as a property of a crossing: we don't want the app
    //    to delete the crossing if the table is not there anymore, so exclude that
    // postboxes are in 4 years category so that postbox collection times is asked instead more often

    private val nodesWaysFilter by lazy { """
        nodes, ways with
          (leisure = pitch and sport = table_tennis)
          and access !~ no|private
          and (${lastChecked(4.0)})
    """.toElementFilterExpression() }

    /* bicycle parkings, motorcycle parkings have capacity quests asked every
    *  few years already, so if it's gone now, it will be noticed that way.
    *  But some users disable this quests as spammy or boring or unimportant,
    *  so asking about this anyway would be a good idea.
    * */

    override val changesetComment = "Check if element still exists"
    override val wikiLink: String? = null
    override val icon = R.drawable.ic_quest_check

    override val questTypeAchievements = listOf(CITIZEN, OUTDOORS)

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsAnyKey("name", "brand", "ref", "operator"))
            R.string.quest_existence_name_title
        else
            R.string.quest_existence_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        return arrayOfNotNull(getNameOrBrandOrOperatorOrRef(tags), featureName.value)
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        (nodesFilter.matches(element) || nodesWaysFilter.matches(element))
        && hasAnyName(element.tags)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry): Sequence<Element> {
        /* put markers for objects that are exactly the same as for which this quest is asking for
           e.g. it's a ticket validator? -> display other ticket validators. Etc. */
        val feature = featureDictionaryFuture.get()
            .byTags(element.tags)
            .isSuggestion(false) // not brands
            .find()
            .firstOrNull() ?: return emptySequence()

        return getMapData().filter { it.tags.containsAll(feature.tags) }.asSequence()
    }

    override fun createForm() = CheckExistenceForm()

    override fun applyAnswerTo(answer: Unit, tags: Tags, timestampEdited: Long) {
        tags.updateCheckDate()
    }

    private fun lastChecked(yearsAgo: Double): String = """
        older today -$yearsAgo years
        or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -$yearsAgo years" }}
    """.trimIndent()

    private fun hasAnyName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}

private fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }
