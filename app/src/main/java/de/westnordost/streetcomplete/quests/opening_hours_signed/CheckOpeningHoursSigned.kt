package de.westnordost.streetcomplete.quests.opening_hours_signed

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys
import de.westnordost.streetcomplete.osm.setCheckDateForKey
import de.westnordost.streetcomplete.osm.toCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.FutureTask

class CheckOpeningHoursSigned(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<Boolean> {

    private val filter by lazy { """
        nodes, ways, relations with
          opening_hours:signed = no
          and (
            $hasOldOpeningHoursCheckDateFilter
            or older today -1 years
          )
          and access !~ private|no
          and (
            name or brand or noname = yes or name:signed = no
            or amenity ~ recycling|toilets|bicycle_rental|charging_station or leisure = park or barrier
          )
    """.toElementFilterExpression() }

    private val hasOldOpeningHoursCheckDateFilter: String get() =
        getLastCheckDateKeys("opening_hours").joinToString("\nor ") {
            "$it < today -1 years"
        }

    override val changesetComment = "Survey whether opening hours are signed"
    override val wikiLink = "Key:opening_hours:signed"
    override val icon = R.drawable.ic_quest_opening_hours_signed
    override val isReplaceShopEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_openingHours_signed_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && hasName(element.tags)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, timestampEdited: Long) {

        if (answer) {
            tags.remove("opening_hours:signed")
            /* it is now signed: we set the check date for the opening hours to the previous edit
               timestamp because this or an older date is the date the opening hours were last
               checked. This is set so that the app will ask about the (signed) opening hours in
               a follow up quest
             */
            val hasCheckDate = getLastCheckDateKeys("opening_hours")
                .any { tags[it]?.toCheckDate() != null }

            if (!hasCheckDate) {
                tags.setCheckDateForKey("opening_hours", Instant.fromEpochMilliseconds(timestampEdited)
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                .date)
            }
        } else {
            tags["opening_hours:signed"] = "no"
            /* still unsigned: just set the check date to now, user was on-site */
            tags.updateCheckDateForKey("opening_hours")
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.containsKey("name") || tags.containsKey("brand")

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
