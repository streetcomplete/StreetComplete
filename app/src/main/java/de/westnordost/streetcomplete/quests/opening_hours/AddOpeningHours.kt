package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupportedOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import de.westnordost.streetcomplete.quests.place_name.PlaceFilterQuestType.OPENING_HOURS_QUEST
import de.westnordost.streetcomplete.quests.place_name.getPlaceElementFilterString
import java.util.concurrent.FutureTask

class AddOpeningHours (
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<OpeningHoursAnswer> {
    private val filter by lazy { """
        nodes, ways, relations with
        (
          (
            (
              shop and shop !~ no|vacant
              or amenity = bicycle_parking and bicycle_parking = building
              or amenity = parking and parking = multi-storey
              or amenity = recycling and recycling_type = centre
              or tourism = information and information = office
              or (amenity = recycling and recycling:batteries = yes)
              or ${getPlaceElementFilterString(OPENING_HOURS_QUEST)}
            )
            and !opening_hours
          )
          or opening_hours older today -1 years
        )
        and access !~ private|no
        and (name or brand or noname = yes or name:signed = no or amenity=recycling)
        and opening_hours:signed != no
    """.toElementFilterExpression() }

    private val nameTags = listOf("name", "brand")

    override val changesetComment = "Add opening hours"
    override val wikiLink = "Key:opening_hours"
    override val icon = R.drawable.ic_quest_opening_hours
    override val isReplaceShopEnabled = true

    override val questTypeAchievements = listOf(CITIZEN)

    override fun getTitle(tags: Map<String, String>): Int {
        val hasProperName = hasProperName(tags)
        val hasFeatureName = hasFeatureName(tags)
        // treat invalid opening hours like it is not set at all
        val hasValidOpeningHours = tags["opening_hours"]?.toOpeningHoursRules() != null
        return if (hasValidOpeningHours) {
            when {
                !hasProperName  -> R.string.quest_openingHours_resurvey_no_name_title
                !hasFeatureName -> R.string.quest_openingHours_resurvey_name_title
                else            -> R.string.quest_openingHours_resurvey_name_type_title
            }
        } else {
            when {
                !hasProperName  -> R.string.quest_openingHours_no_name_title
                !hasFeatureName -> R.string.quest_openingHours_name_title
                else            -> R.string.quest_openingHours_name_type_title
            }
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

    override fun isApplicableTo(element: Element) : Boolean {
        if (!filter.matches(element)) return false
        val tags = element.tags
        // only show places that can be named somehow
        if (!hasName(tags)) return false
        // no opening_hours yet -> new survey
        val oh = tags["opening_hours"] ?: return true
        // invalid opening_hours rules -> applicable because we want to ask for opening hours again
        val rules = oh.toOpeningHoursRules() ?: return true
        // only display supported rules
        return rules.isSupportedOpeningHours()
    }

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter("nodes, ways, relations with " + isKindOfShopExpression())

    override fun createForm() = AddOpeningHoursForm()

    override fun applyAnswerTo(answer: OpeningHoursAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is RegularOpeningHours -> {
                changes.updateWithCheckDate("opening_hours", answer.hours.toString())
                changes.deleteIfPreviously("opening_hours:signed", "no")
            }
            is AlwaysOpen          -> {
                changes.updateWithCheckDate("opening_hours", "24/7")
                changes.deleteIfPreviously("opening_hours:signed", "no")
            }
            is DescribeOpeningHours -> {
                val text = answer.text.replace("\"","")
                changes.updateWithCheckDate("opening_hours", "\"$text\"")
                changes.deleteIfPreviously("opening_hours:signed", "no")
            }
            is NoOpeningHoursSign  -> {
                changes.addOrModify("opening_hours:signed", "no")
                // don't delete current opening hours: these may be the correct hours, they are just not visible anywhere on the door
            }
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(nameTags)

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
