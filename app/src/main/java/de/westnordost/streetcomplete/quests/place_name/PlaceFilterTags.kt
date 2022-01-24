package de.westnordost.streetcomplete.quests.place_name

import de.westnordost.streetcomplete.quests.place_name.PlaceFilterQuestType.OPENING_HOURS_QUEST
import de.westnordost.streetcomplete.quests.place_name.PlaceFilterQuestType.PLACE_NAME_QUEST
import de.westnordost.streetcomplete.quests.place_name.PlaceFilterQuestType.WHEELCHAIR_ACCESS_QUEST

enum class PlaceFilterQuestType {
    OPENING_HOURS_QUEST,
    PLACE_NAME_QUEST,
    WHEELCHAIR_ACCESS_QUEST,
}

private typealias ValuesPerQuestTypes = Map<Set<PlaceFilterQuestType>, List<String>>

fun getPlaceElementFilterString(questType: PlaceFilterQuestType): String {
    val common = setOf(OPENING_HOURS_QUEST, PLACE_NAME_QUEST, WHEELCHAIR_ACCESS_QUEST)
    val nameAndOpeningHours = setOf(PLACE_NAME_QUEST, OPENING_HOURS_QUEST)
    val nameAndWheelchair = setOf(PLACE_NAME_QUEST, WHEELCHAIR_ACCESS_QUEST)
    val name = setOf(PLACE_NAME_QUEST)
    val wheelchair = setOf(WHEELCHAIR_ACCESS_QUEST)

    val filterTags: Map<String, ValuesPerQuestTypes> = mapOf(
        "amenity" to mapOf(
            common to listOf(
                // eat & drink
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten",
                "food_court", "nightclub",
                // amenities
                "cinema", "planetarium", "casino",
                // civic
                "townhall", "courthouse", "embassy", "community_centre", "youth_centre", "library",
                // commercial
                "bank", "bureau_de_change", "money_transfer", "post_office", "marketplace",
                "internet_cafe",
                // car stuff
                "car_wash", "car_rental", "fuel",
                // health
                "dentist", "doctors", "clinic", "pharmacy", "veterinary",
                // animals
                "animal_boarding", "animal_shelter", "animal_breeding",
            ),
            nameAndOpeningHours to listOf(
                "boat_rental",
            ),
            nameAndWheelchair to listOf(
                // culture
                "theatre",
                // events
                "conference_centre", "arts_centre",
                // civic
                "police", "ranger_station",
                // transport
                "ferry_terminal",
                // religious
                "place_of_worship",
                // health care
                "hospital",
            ),
            name to listOf(
                // culture
                "studio",
                // events
                "events_venue", "exhibition_centre", "music_venue",
                // civic
                "prison", "fire_station",
                // social
                "social_facility", "nursing_home", "childcare", "retirement_home", "social_centre",
                // religious
                "monastery",
                // education
                "kindergarten", "school", "college", "university", "research_institute",
                // learning
                "driving_school", "dive_centre", "language_school", "music_school",
                // bad stuff
                "brothel", "gambling", "love_hotel", "stripclub",
            ),

            // openingHours:
            // not ATM because too often it's simply 24/7 and too often it is confused with
            // a bank that might be just next door because the app does not tell the user what
            // kind of object this is about
        ),
        "tourism" to mapOf(
            common to listOf(
                "zoo", "aquarium", "theme_park", "gallery", "museum",
                // plus `tourism = information and information = office`
            ),
            nameAndWheelchair to listOf(
                "attraction",
                // accommodations
                "hotel", "guest_house", "motel", "hostel", "alpine_hut", "apartment", "resort",
                "camp_site", "caravan_site", "chalet",
            ),
            wheelchair to listOf(
                "viewpoint",
            ),
        ),
        "leisure" to mapOf(
            common to listOf(
                "fitness_centre", "golf_course", "water_park", "miniature_golf", "bowling_alley",
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",
            ),
            nameAndWheelchair to listOf(
                "sports_centre", "stadium",
            ),
            nameAndOpeningHours to listOf(
                "horse_riding",
            ),
            name to listOf(
                "dance", "nature_reserve", "marina",
            ),

            // openingHours:
            // not sports_centre, dance etc because these are often sports clubs which have no
            // walk-in opening hours but training times
        ),
        "office" to mapOf(
            common to listOf(
                "insurance", "government", "travel_agent", "tax_advisor", "religion",
                "employment_agency", "diplomatic",
            ),
            nameAndWheelchair to listOf(
                "lawyer", "estate_agent", "political_party", "therapist",
            ),
            // name: ask for all offices
        ),
        "craft" to mapOf(
            common to listOf(
                "carpenter", "shoemaker", "tailor", "photographer", "dressmaker",
                "electronics_repair", "key_cutter", "stonemason",
            ),
            nameAndWheelchair to listOf(
                "winery",
            ),
            // name: ask for all crafts
        ),
    )

    return filterTags.mapNotNull { (key, valuesPerQuestType) ->
        valuesPerQuestType.forQuestType(questType)?.let { values ->
            "$key ~ ${values.joinToString("|")}"
        }
    }.joinToString(" or ")
}

private fun ValuesPerQuestTypes.forQuestType(questType: PlaceFilterQuestType): List<String>? =
    this.entries
        .filter { questType in it.key }
        .flatMap { it.value }
        .ifEmpty { null }
