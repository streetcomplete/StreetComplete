package de.westnordost.streetcomplete.quests.opening_hours_signed

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.geometry.ElementGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.getLastCheckDateKeys
import de.westnordost.streetcomplete.osm.isPlaceOrDisusedPlace
import de.westnordost.streetcomplete.osm.setCheckDateForKey
import de.westnordost.streetcomplete.osm.toCheckDate
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import de.westnordost.streetcomplete.quests.YesNoQuestForm
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class CheckOpeningHoursSigned(
    private val getFeature: (Element) -> Feature?
) : OsmElementQuestType<Boolean> {

    private val filter by lazy { """
        nodes, ways with
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
    override val isReplacePlaceEnabled = true
    override val achievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>) = R.string.quest_openingHours_signed_title

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && hasName(element)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().asSequence().filter { it.isPlaceOrDisusedPlace() }

    override fun createForm() = YesNoQuestForm()

    override fun applyAnswerTo(answer: Boolean, tags: Tags, geometry: ElementGeometry, timestampEdited: Long) {
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
            // still unsigned: just set the check date to now, user was on-site
            tags.updateCheckDateForKey("opening_hours")
        }
    }

    private fun hasName(element: Element) = hasProperName(element.tags) || hasFeatureName(element)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.containsKey("name") || tags.containsKey("brand")

    private fun hasFeatureName(element: Element) = getFeature(element)?.name != null
}
