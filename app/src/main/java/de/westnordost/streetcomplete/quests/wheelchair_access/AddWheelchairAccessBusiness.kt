package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.meta.OsmTaggings
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddWheelchairAccessBusiness(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o)
{
    override val tagFilters = """
        nodes, ways, relations with
        (
         shop and shop !~ no|vacant
         or amenity = parking and parking = multi-storey
         or amenity = recycling and recycling_type = centre
         or tourism = information and information = office
         or """.trimIndent() +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "wheelchair only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub", // eat & drink
                "cinema", "planetarium", "casino",                                                                     // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",                    // civic
                "bank", /*atm,*/ "bureau_de_change", "money_transfer", "post_office", "marketplace", "internet_cafe",  // commercial
                "car_wash", "car_rental", "boat_rental", "fuel",                                                       // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                                              // health

                // wheelchair only
                "theatre", "conference_centre", "arts_centre",
                "police", "ranger_station","prison",
                "kindergarten", "school", "college", "university", "research_institute",                               // education
                "driving_school", "dive_centre", "language_school", "music_school",                                    // learning
                "ferry_terminal",
                "place_of_worship",
                "hospital"
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",
                // wheelchair only
                "attraction", "viewpoint",
                "hotel", "guest_house", "hostel", "motel", "apartment", "chalet"
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "dance", "golf_course", "water_park",
                "miniature_golf", "bowling_alley", "horse_riding",  "amusement_arcade",
                "adult_gaming_centre", "tanning_salon",
                // wheelchair only
                "sports_centre"
            ),
            "office" to arrayOf(
                // common
                "insurance", "government", "travel_agent", "tax_advisor", "religion", "employment_agency",

                // name and wheelchair
                "lawyer", "estate_agent", "political_party", "therapist"
            ),
            "craft" to arrayOf(
                // common
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason",

                // name and wheelchair
                "winery"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "\n) and !wheelchair and name"

    override val commitMessage = "Add wheelchair access"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_name_title

    override fun createForm() = AddWheelchairAccessBusinessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("wheelchair", answer)
    }
}
