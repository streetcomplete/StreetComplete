package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.osmapi.map.MapDataWithGeometry
import de.westnordost.osmapi.map.data.Element
import de.westnordost.osmfeatures.FeatureDictionary
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.meta.updateWithCheckDate
import de.westnordost.streetcomplete.data.osm.osmquest.OsmElementQuestType
import de.westnordost.streetcomplete.ktx.containsAny
import de.westnordost.streetcomplete.quests.opening_hours.parser.toOpeningHoursRows
import de.westnordost.streetcomplete.quests.opening_hours.parser.toOpeningHoursRules
import java.util.concurrent.FutureTask

class AddOpeningHours (
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
              or tourism = information and information = office
              or """.trimIndent() +

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
                "insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency"
            ),
            "craft" to arrayOf(
                // common
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") + "\n" + """
            )
            and !opening_hours
          )
          or opening_hours older today -1 years
        )
        and (access !~ private|no)
        and (name or brand or noname = yes)
        and opening_hours:signed != no
    """.trimIndent()).toElementFilterExpression() }

    override val commitMessage = "Add opening hours"
    override val wikiLink = "Key:opening_hours"
    override val icon = R.drawable.ic_quest_opening_hours

    override fun getTitle(tags: Map<String, String>): Int {
        val hasProperName = hasProperName(tags)
        val hasNonBrandFeatureName = hasFeatureName(tags) && !tags.containsKey("brand")
        // treat invalid opening hours like it is not set at all
        val hasValidOpeningHours = tags["opening_hours"]?.toOpeningHoursRules() != null
        return if (hasValidOpeningHours) {
            when {
                !hasProperName          -> R.string.quest_openingHours_resurvey_no_name_title
                !hasNonBrandFeatureName -> R.string.quest_openingHours_resurvey_name_title
                else                    -> R.string.quest_openingHours_resurvey_name_type_title
            }
        } else {
            when {
                !hasProperName          -> R.string.quest_openingHours_no_name_title
                !hasNonBrandFeatureName -> R.string.quest_openingHours_name_title
                else                    -> R.string.quest_openingHours_name_type_title
            }
        }
    }

    override fun getTitleArgs(tags: Map<String, String>, featureName: Lazy<String?>): Array<String> {
        val name = tags["name"] ?: tags["brand"]
        val hasProperName = name != null
        val hasNonBrandFeatureName = hasFeatureName(tags) && !tags.containsKey("brand")
        return when {
            !hasProperName          -> arrayOf(featureName.value.toString())
            !hasNonBrandFeatureName -> arrayOf(name!!)
            else                    -> arrayOf(name!!, featureName.value.toString())
        }
    }

    override fun getApplicableElements(mapData: MapDataWithGeometry): Iterable<Element> =
        mapData.filter { isApplicableTo(it) }

    override fun isApplicableTo(element: Element) : Boolean {
        if (!filter.matches(element)) return false
        // only show places that can be named somehow
        if (!hasName(element.tags)) return false
        // no opening_hours yet -> new survey
        val oh = element.tags?.get("opening_hours") ?: return true
        // invalid opening_hours rules -> applicable because we want to ask for opening hours again
        val rules = oh.toOpeningHoursRules() ?: return true
        // only display supported rules
        return rules.toOpeningHoursRows() != null
    }

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

    private fun hasName(tags: Map<String, String>?) = hasProperName(tags) || hasFeatureName(tags)

    private fun hasProperName(tags: Map<String, String>?): Boolean =
        tags?.keys?.containsAny(listOf("name", "brand")) ?: false

    private fun hasFeatureName(tags: Map<String, String>?): Boolean =
        tags?.let { featureDictionaryFuture.get().byTags(it).find().isNotEmpty() } ?: false
}
