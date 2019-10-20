package de.westnordost.streetcomplete.quests.opening_hours

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder

class AddOpeningHours (o: OverpassMapDataDao) : SimpleOverpassQuestType<OpeningHoursAnswer>(o) {

    /* See also AddWheelchairAccessBusiness and AddPlaceName, which has a similar list and is/should
       be ordered in the same way for better overview */
    override val tagFilters = """
        nodes, ways, relations with
        (
         shop and shop !~ no|vacant
         or amenity = bicycle_parking and bicycle_parking = building
         or amenity = parking and parking = multi-storey
         or amenity = recycling and recycling_type = centre
         or tourism = information and information = office
         or """.trimIndent() +
        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino", "library",                                                          // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre",                               // civic
                // not ATM because too often it's simply 24/7 and too often it is confused with
                // a bank that might be just next door because the app does not tell the user what
                // kind of object this is about
                "bank", /*atm,*/ "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",  // commercial
                "car_wash", "car_rental", "boat_rental", "fuel",                                                       // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary"                                               // health
            ),
            "tourism" to arrayOf(
                "zoo", "aquarium", "theme_park", "gallery", "museum"
                // and tourism=information, see above
            ),
            "leisure" to arrayOf(
                // not sports_centre because these are often sports clubs which have no walk-in
                // opening hours but training times
                "fitness_centre", "dance", "golf_course", "water_park",
                "miniature_golf", "bowling_alley", "horse_riding",  "amusement_arcade",
                "adult_gaming_centre", "tanning_salon"
            ),
            "office" to arrayOf(
                // also listed for AddWheelchair quest
                "insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency"
            ),
            "craft" to arrayOf(
                // also listed for AddWheelchair quest
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") + "\n" + """
        )
         and !opening_hours and name and opening_hours:signed != no
         and (access !~ private|no)
        """.trimIndent()

    override val commitMessage = "Add opening hours"
    override val icon = R.drawable.ic_quest_opening_hours

    override fun getTitle(tags: Map<String, String>) = R.string.quest_openingHours_name_title

    override fun createForm() = AddOpeningHoursForm()

    override fun applyAnswerTo(answer: OpeningHoursAnswer, changes: StringMapChangesBuilder) {
        when(answer) {
            is RegularOpeningHours -> changes.add("opening_hours", answer.times.joinToString(";"))
            is AlwaysOpen          -> changes.add("opening_hours", "24/7")
            is NoOpeningHoursSign  -> changes.add("opening_hours:signed", "no")
            is DescribeOpeningHours -> {
                val text = answer.text.replace("\"","")
                changes.add("opening_hours", "\"$text\"")
            }
        }
    }
}
