package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.isKindOfShopExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmElementQuestType
import de.westnordost.streetcomplete.data.osm.osmquests.Tags
import de.westnordost.streetcomplete.data.user.achievements.QuestTypeAchievement.CITIZEN
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.osm.opening_hours.parser.isSupportedOpeningHours
import de.westnordost.streetcomplete.osm.opening_hours.parser.toOpeningHoursRules
import java.util.concurrent.FutureTask

class AddOpeningHours(
    private val featureDictionaryFuture: FutureTask<FeatureDictionary>
) : OsmElementQuestType<OpeningHoursAnswer> {

    /* See also AddWheelchairAccessBusiness and AddPlaceName, which has a similar list and is/should
       be ordered in the same way for better overview */
    private val filter by lazy { ("""
        nodes, ways, relations with
        (
          (
            (
              shop and shop !~ no|vacant
              or amenity = bicycle_parking and bicycle_parking = building
              or amenity = parking and parking = multi-storey
              or amenity = recycling and recycling_type = centre
              or amenity = social_facility and social_facility ~ food_bank|clothing_bank|soup_kitchen|dairy_kitchen
              or tourism = information and information = office
              or """ +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "opening_hours only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",                    // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",           // commercial
                "car_wash", "car_rental", "fuel",                                                                      // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health
                "animal_boarding", "animal_shelter", "animal_breeding",                                                // animals
                "coworking_space",                                                                                     // work

                // name & opening hours
                "boat_rental"

                // not ATM because too often it's simply 24/7 and too often it is confused with
                // a bank that might be just next door because the app does not tell the user what
                // kind of object this is about
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum"
                // and tourism = information, see above
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",

                // name & opening hours
                "horse_riding"

                // not sports_centre, dance etc because these are often sports clubs which have no
                // walk-in opening hours but training times
            ),
            "office" to arrayOf(
                // common
                "insurance", "government", "travel_agent", "tax_advisor", "religion",
                "employment_agency", "diplomatic", "coworking",
            ),
            "craft" to arrayOf(
                // common
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason"
            ),
            "healthcare" to arrayOf(
                // common
                "audiologist", "optometrist", "counselling", "speech_therapist",
                "sample_collection", "blood_donation",

                // name & opening hours
                "physiotherapist", "podiatrist",
            ),
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") + "\n" + """
            )
            and (!opening_hours or opening_hours older today -1 years)
          )
          or (
            opening_hours older today -1 years
            and (
              leisure = park
              or barrier
              or amenity ~ toilets|bicycle_rental|charging_station
            )
          )
        )
        and access !~ private|no
        and (
          name or brand or noname = yes or name:signed = no
          or barrier
          or amenity ~ toilets|bicycle_rental
        )
        and opening_hours:signed != no
    """).toElementFilterExpression() }

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

    override fun isApplicableTo(element: Element): Boolean {
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
        getMapData().filter("nodes, ways, relations with " +
            isKindOfShopExpression() + " or " + isKindOfShopExpression("disused")
        )

    override fun createForm() = AddOpeningHoursForm()

    override fun applyAnswerTo(answer: OpeningHoursAnswer, tags: Tags, timestampEdited: Long) {
        if (answer is NoOpeningHoursSign) {
            tags["opening_hours:signed"] = "no"
            // don't delete current opening hours: these may be the correct hours, they are just not visible anywhere on the door
        } else {
            val openingHoursString = when (answer) {
                is RegularOpeningHours  -> answer.hours.toString()
                is AlwaysOpen           -> "24/7"
                is DescribeOpeningHours -> "\"" + answer.text.replace("\"", "") + "\""
                NoOpeningHoursSign      -> throw IllegalStateException()
            }
            tags.updateWithCheckDate("opening_hours", openingHoursString)
            if (tags["opening_hours:signed"] == "no") {
                tags.remove("opening_hours:signed")
            }
        }
    }

    private fun hasName(tags: Map<String, String>) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>): Boolean =
        tags.keys.containsAny(nameTags)

    private fun hasFeatureName(tags: Map<String, String>): Boolean =
        featureDictionaryFuture.get().byTags(tags).isSuggestion(false).find().isNotEmpty()
}
