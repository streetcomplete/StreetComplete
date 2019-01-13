package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddWheelchairAccessBusiness(o: OverpassMapDataDao) : SimpleOverpassQuestType<String>(o)
{
    override val tagFilters =
        " nodes, ways, relations with ( shop and shop !~ no|vacant" +
        " or amenity = parking and parking = multi-storey" +
        " or amenity = recycling and recycling_type = centre" +
        " or tourism = information and information = office" +
        " or  " +
        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub",
                "cinema", "library", "theatre", "arts_centre", "casino", "conference_centre",
                "bank", "bureau_de_change", "money_transfer", "post_office", "internet_cafe", "marketplace",
                "police", "ranger_station", "courthouse", "embassy", "townhall", "community_centre", "youth_centre",
                "car_wash", "car_rental", "fuel", "driving_school",
                "doctors", "clinic", "pharmacy", "veterinary", "dentist",
                "place_of_worship"
            ),
            "tourism" to arrayOf(
                "zoo", "aquarium", "theme_park", "gallery", "attraction", "viewpoint",
                "museum", "hotel", "guest_house", "hostel", "motel", "apartment", "chalet"
            ),
            "leisure" to arrayOf(
                "golf_course", "water_park", "miniature_golf", "dance",
                "bowling_alley", "horse_riding", "sports_centre", "fitness_centre",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon"
            ),
            "office" to arrayOf(
                "insurance", "government", "lawyer", "estate_agent", "political_party", "travel_agent",
                "tax_advisor", "therapist"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString(" or ") +
        " )" +
        " and !wheelchair and name"

    override val commitMessage = "Add wheelchair access"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_name_title

    override fun createForm() = AddWheelchairAccessBusinessForm()

    override fun applyAnswerTo(answer: String, changes: StringMapChangesBuilder) {
        changes.add("wheelchair", answer)
    }
}
