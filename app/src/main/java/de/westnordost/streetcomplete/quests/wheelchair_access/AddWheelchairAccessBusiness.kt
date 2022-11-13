package de.westnordost.streetcomplete.quests.wheelchair_access

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.data.osm.mapdata.MapDataWithGeometry
import de.westnordost.streetcomplete.data.osm.mapdata.filter
import de.westnordost.streetcomplete.data.osm.osmquests.OsmFilterQuestType
import de.westnordost.streetcomplete.data.user.achievements.EditTypeAchievement.WHEELCHAIR
import de.westnordost.streetcomplete.osm.IS_SHOP_OR_DISUSED_SHOP_EXPRESSION
import de.westnordost.streetcomplete.osm.Tags

class AddWheelchairAccessBusiness : OsmFilterQuestType<WheelchairAccess>() {

    override val elementFilter = """
        nodes, ways with
          access !~ no|private
          and !wheelchair
          and (name or brand or name:signed = no)
          and (
            shop and shop !~ no|vacant
            or amenity = parking and parking = multi-storey
            or amenity = recycling and recycling_type = centre
            or amenity = social_facility and social_facility ~ food_bank|clothing_bank|soup_kitchen|dairy_kitchen
            or tourism = information and information = office
            or """ +

        // The common list is shared by the name quest, the opening hours quest and the wheelchair quest.
        // So when adding other tags to the common list keep in mind that they need to be appropriate for all those quests.
        // Independent tags can by added in the "wheelchair only" tab.

        mapOf(
            "amenity" to arrayOf(
                // common
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten",         // eat & drink
                "food_court", "nightclub",
                "cinema", "planetarium", "casino",                                                  // amenities
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library", // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace",         // commercial
                "internet_cafe", "payment_centre",
                "car_wash", "car_rental", "fuel",                                                   // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",                           // health
                "animal_boarding", "animal_shelter", "animal_breeding",                             // animals
                "coworking_space",                                                                  // work

                // name & wheelchair only
                "theatre",                             // culture
                "conference_centre", "arts_centre",    // events
                "police", "ranger_station",            // civic
                "ferry_terminal",                      // transport
                "place_of_worship",                    // religious
                "hospital"                             // health care
            ),
            "tourism" to arrayOf(
                // common
                "zoo", "aquarium", "theme_park", "gallery", "museum",

                // name & wheelchair
                "attraction",
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site", "chalet", // accommodations

                // wheelchair only
                "viewpoint"

                // and tourism = information, see above
            ),
            "leisure" to arrayOf(
                // common
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",

                // name & wheelchair
                "sports_centre", "stadium"
            ),
            "office" to arrayOf(
                // common
                "insurance", "government", "travel_agent", "tax_advisor", "religion",
                "employment_agency", "diplomatic", "coworking",
                "estate_agent", "lawyer", "telecommunication", "educational_institution",
                "association", "ngo", "it", "accountant",

                // name & wheelchair
                "political_party", "therapist"
            ),
            "craft" to arrayOf(
                // common
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason",

                // name & wheelchair
                "winery"
            ),
            "healthcare" to arrayOf(
                // common
                "pharmacy", "doctor", "clinic", "dentist", "centre", "physiotherapist",
                "laboratory", "alternative", "psychotherapist", "optometrist", "podiatrist",
                "nurse", "counselling", "speech_therapist", "blood_donation", "sample_collection",
                "occupational_therapist", "dialysis", "vaccination_centre", "audiologist",
                "blood_bank", "nutrition_counselling",

                // name & wheelchair
                "rehabilitation", "hospice", "midwife", "birthing_centre"
            ),
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n or ") +
        "  \n)"

    override val changesetComment = "Survey wheelchair accessibility of places"
    override val wikiLink = "Key:wheelchair"
    override val icon = R.drawable.ic_quest_wheelchair_shop
    override val isReplaceShopEnabled = true
    override val achievements = listOf(WHEELCHAIR)
    override val defaultDisabledMessage = R.string.default_disabled_msg_go_inside

    override fun getTitle(tags: Map<String, String>) = R.string.quest_wheelchairAccess_outside_title

    override fun getHighlightedElements(element: Element, getMapData: () -> MapDataWithGeometry) =
        getMapData().filter(IS_SHOP_OR_DISUSED_SHOP_EXPRESSION)

    override fun createForm() = AddWheelchairAccessBusinessForm()

    override fun applyAnswerTo(answer: WheelchairAccess, tags: Tags, timestampEdited: Long) {
        tags["wheelchair"] = answer.osmValue
    }
}
