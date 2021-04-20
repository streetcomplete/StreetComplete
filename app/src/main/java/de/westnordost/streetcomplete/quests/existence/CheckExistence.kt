package de.westnordost.streetcomplete.quests.existence

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.LAST_CHECK_DATE_KEYS
import de.westnordost.streetcomplete.data.meta.SURVEY_MARK_KEY
import de.westnordost.streetcomplete.data.meta.toCheckDateString
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.arrayOfNotNull
import de.westnordost.streetcomplete.ktx.containsAnyKey
import java.time.LocalDate
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
            or amenity = bench
            or amenity = waste_basket
            or amenity = post_box
            or amenity = grit_bin
            or leisure = picnic_table
            or leisure = firepit
            or amenity = vending_machine and vending ~ parking_tickets|public_transport_tickets
            or tourism = information and information ~ board|terminal|map
            or advertising ~ column|board|poster_box
            or traffic_calming ~ bump|hump|island|cushion|choker|rumble_strip|chicane|dip
            or traffic_calming = table and !highway and !crossing
          )
          and (${lastChecked(4.0)})
        )) and access !~ no|private
    """.toElementFilterExpression()
    }
    // traffic_calming = table is often used as a property of a crossing: we don't want the app
    //    to delete the crossing if the table is not there anymore, so exclude that
    // postboxes are in 4 years category so that postbox collection times is asked instead more often

    private val nodesWaysFilter by lazy { """
        nodes, ways with (
            leisure = pitch and sport = table_tennis
        )
        and access !~ no|private
        and (${lastChecked(4.0)})
    """.toElementFilterExpression() }

    /* not including bicycle parkings, motorcycle parkings because their capacity is asked every
    *  few years already, so if it's gone now, it will be noticed that way. */

    override val commitMessage = "Check if element still exists"
    override val wikiLink: String? = null
    override val icon = R.drawable.ic_quest_check

    override fun getTitle(tags: Map<String, String>): Int =
        if (tags.containsAnyKey("name", "brand", "operator"))
            R.string.quest_existence_name_title
        else
            R.string.quest_existence_title

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"] ?: tags["operator"]
        return arrayOfNotNull(name, featureName.value)
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) =
        (nodesFilter.matches(element) || nodesWaysFilter.matches(element))
        && hasAnyName(element.tags)

    override fun createForm() = CheckExistenceForm()

    override fun applyAnswerTo(answer: Unit, changes: StringMapChangesBuilder) {
        changes.addOrModify(SURVEY_MARK_KEY, LocalDate.now().toCheckDateString())
        val otherCheckDateKeys = LAST_CHECK_DATE_KEYS.filterNot { it == SURVEY_MARK_KEY }
        for (otherCheckDateKey in otherCheckDateKeys) {
            changes.deleteIfExists(otherCheckDateKey)
        }
    }

    private fun lastChecked(yearsAgo: Double): String = """
        older today -$yearsAgo years
        or ${LAST_CHECK_DATE_KEYS.joinToString(" or ") { "$it < today -$yearsAgo years" }}
    """.trimIndent()

    private fun hasAnyName(tags: Map<String, String>?): Boolean =
        tags?.let { featureDictionaryFuture.get().byTags(it).find().isNotEmpty() } ?: false
}
