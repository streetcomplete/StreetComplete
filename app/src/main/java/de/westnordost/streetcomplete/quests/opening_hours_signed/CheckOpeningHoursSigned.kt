package de.westnordost.streetcomplete.quests.opening_hours_signed

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.data.meta.getLastCheckDateKeys
import de.westnordost.streetcomplete.data.meta.setCheckDateForKey
import de.westnordost.streetcomplete.data.meta.toCheckDate
import de.westnordost.streetcomplete.data.meta.updateCheckDateForKey
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.quests.YesNoQuestAnswerFragment
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.FutureTask

class CheckOpeningHoursSigned (
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
            or amenity ~ recycling|toilets|bicycle_rental|charging_station or leisure=park or barrier
          )
    """.toElementFilterExpression() }

    private val hasOldOpeningHoursCheckDateFilter: String get() =
        getLastCheckDateKeys("opening_hours").joinToString("\nor ") {
            "$it < today -1 years"
        }

    private val nameTags = listOf("name", "brand")

    override val changesetComment = "Check whether opening hours are signed"
    override val wikiLink = "Key:opening_hours:signed"
    override val icon = R.drawable.ic_quest_opening_hours_signed
    override val isReplaceShopEnabled = true
    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>): Int {
        val hasProperName = hasProperName(tags)
        val hasFeatureName = hasFeatureName(tags)
        return  when {
            !hasProperName  -> R.string.quest_openingHours_signed_no_name_title
            !hasFeatureName -> R.string.quest_openingHours_signed_name_title
            else            -> R.string.quest_openingHours_signed_name_type_title
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        val hasProperName = name != null
        val hasFeatureName = hasFeatureName(tags)
        return when {
            !hasProperName  -> arrayOf(featureName.value.toString())
            !hasFeatureName -> arrayOf(name!!)
            else            -> arrayOf(name!!, featureName.value.toString())
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element): Boolean =
        filter.matches(element) && hasName(element.tags)

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = YesNoQuestAnswerFragment()

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
                tags.setCheckDateForKey("opening_hours", LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestampEdited), ZoneId.systemDefault()
                ).toLocalDate())
            }
        } else {
            tags["opening_hours:signed"] = "no"
            /* still unsigned: just set the check date to now, user was on-site */
            tags.updateCheckDateForKey("opening_hours")
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(nameTags)

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
