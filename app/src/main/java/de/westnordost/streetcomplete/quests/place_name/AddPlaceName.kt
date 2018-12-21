package de.westnordost.streetcomplete.quests.place_name

import android.os.Bundle

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.osm.SimpleOverpassQuestType
import de.westnordost.streetcomplete.data.osm.changes.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.download.OverpassMapDataDao

class AddPlaceName(o: OverpassMapDataDao) : SimpleOverpassQuestType(o) {

    override val tagFilters =
        "nodes, ways, relations with !name and noname != yes " +
        " and (shop and shop !~ no|vacant or tourism = information and information = office " +
        " or " +
        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "food_court", "nightclub",                  // eat & drink
                "cinema", "theatre", "planetarium", "arts_centre", "studio",                                                            // culture
                "events_venue", "conference_centre", "exhibition_centre", "music_venue",                                                // events
                "townhall", "prison", "courthouse", "embassy", "police", "fire_station", "ranger_station",                              // civic
                "bank", "bureau_de_change", "money_transfer", "post_office", "library", "marketplace", "internet_cafe",                 // commercial
                "community_centre", "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre", "youth_centre", // social
                "car_wash", "car_rental", "boat_rental", "fuel",                                                                        // car stuff
                "dentist", "doctors", "clinic", "pharmacy", "hospital",                                                                 // health care
                "place_of_worship", "monastery",                                                                                        // religious
                "kindergarten", "school", "college", "university", "research_institute",                                                // education
                "driving_school", "dive_centre", "language_school", "music_school",                                                     // learning
                "casino", "brothel", "gambling", "love_hotel", "stripclub",                                                             // bad stuff
                "animal_boarding", "animal_shelter", "animal_breeding", "veterinary"
            ),
            "tourism" to arrayOf(
                "attraction", "zoo", "aquarium", "theme_park", "gallery", "museum",                                          // attractions
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort", "camp_site", "caravan_site"  // accomodations
                // and tourism=information, see above
            ),
            "leisure" to arrayOf(
                "park", "nature_reserve", "sports_centre", "fitness_centre", "dance", "golf_course",
                "water_park", "miniature_golf", "stadium", "marina", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon", "horse_riding"
            ),
            "office" to arrayOf(
                "insurance", "estate_agent", "travel_agent"
            )
        ).map { it.key + " ~ " + it.value.joinToString("|") }.joinToString(" or ") +
        ")"

    override val commitMessage = "Determine place names"
    override val icon = R.drawable.ic_quest_label

    override fun getTitle(tags: Map<String, String>) = R.string.quest_placeName_title

    override fun createForm() = AddPlaceNameForm()

    override fun applyAnswerTo(answer: Bundle, changes: StringMapChangesBuilder) {
        if (answer.getBoolean(AddPlaceNameForm.NO_NAME)) {
            changes.add("noname", "yes")
        } else {
            changes.add("name", answer.getString(AddPlaceNameForm.NAME)!!)
        }
    }
}
