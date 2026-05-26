package de.westnordost.streetcomplete.osm.places

import de.westnordost.osmfeatures.Feature
import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element
import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.applyTo
import de.westnordost.streetcomplete.osm.asIfItWasnt
import de.westnordost.streetcomplete.osm.featureId
import de.westnordost.streetcomplete.osm.isChildOf
import de.westnordost.streetcomplete.osm.localized_name.LocalizedName
import de.westnordost.streetcomplete.osm.removeCheckDates

/** Return whether this element is a kind of place, regardless whether it is currently vacant or
 *  not */
fun Element.isPlaceOrDisusedPlace(): Boolean =
    isPlace() || isDisusedPlace()

/** Return whether this element is a kind of disused or vacant place */
fun Element.isDisusedPlace(): Boolean =
    IS_VACANT_PLACE_EXPRESSION.matches(this) ||
    this.asIfItWasnt("disused")?.let { IS_PLACE_EXPRESSION.matches(it) } == true

fun Element.isPlace(): Boolean =
    IS_PLACE_EXPRESSION.matches(this)

/** Map features like shops or amenities that usually have a name and can be entered.
 *
 *  To demarcate this category from others, the following are not included:
 *  - things that are rather structures or buildings than places (e.g. communication towers, train station)
 *  - landuses, outside-things (golf courses, zoos, marketplaces, car parking)
 *
 *  Note: When this function is modified, please follow update instructions in:
 *        https://github.com/mnalis/StreetComplete-taginfo-categorize/blob/master/README.md
 *
 *  Important: The contribution policy here is that only things may be added if an iD preset for it
 *             exists. This policy exists in order to reduce effort to maintain this list, i.e. we
 *             don't want to check, weigh and balance requests in parallel to iD maintainers (in
 *             terms of notability, it being unambiguous, consensus etc.)
 */
private val IS_PLACE_EXPRESSION by lazy {
    val tags = mapOf(
        "amenity" to listOf(
            // grouped by subcategory, sorted by alphabet

            // sustenance
            "bar",
            "biergarten",
            "cafe",
            "fast_food",
            "food_court",
            "ice_cream",
            "pub",
            "restaurant",

            // education
            "childcare",
            "college",
            "dancing_school",
            "dive_centre",
            "dojo",
            "driving_school",
            "kindergarten",
            "language_school",
            "library",
            "music_school",
            "prep_school",
            "research_institute",
            "school",
            "toy_library",
            "training",
            "university",

            // transportation
            "boat_rental", // usually there is some kind of office, even if it is just a small stall
            "car_rental", // usually there is some kind of office
            "car_wash",
            "fuel",
            "motorcycle_rental",
            "vehicle_inspection",

            // financial
            "bank",
            "bureau_de_change",
            "mobile_money_agent",
            "money_transfer",
            "payment_centre",

            // healthcare
            "clinic",
            "dentist",
            "doctors",
            "health_post",
            "hospital",
            "nursing_home",
            "pharmacy",
            "social_facility",
            "veterinary",
            "veterinary_pharmacy",

            // entertainment, arts & culture
            "archive",
            "arts_centre",
            "brothel",
            "casino",
            "cinema",
            "community_centre",
            "conference_centre",
            "events_venue",
            "exhibition_centre",
            "gambling",
            "hookah_lounge",
            "love_hotel",
            "music_venue",
            "nightclub",
            "planetarium",
            "ski_rental",
            "social_centre",
            "stripclub",
            "studio",
            "swingerclub",
            "theatre",

            // public service
            "courthouse",
            "embassy", // deprecated now
            "fire_station",
            "mailroom",
            "police",
            "post_depot",
            "post_office",
            "prison",
            "ranger_station",
            "townhall",

            // facilities
            "lavoir",
            "left_luggage",

            // others
            "animal_boarding",
            "animal_shelter",
            "animal_training",
            "coworking_space", // basically an office
            "crematorium",
            "funeral_hall",
            "internet_cafe",
            "meditation_centre",
            "monastery",
            "mortuary",
            "place_of_mourning",
            "place_of_worship",
            "public_bath",
        ),
        "education" to listOf(
            "college",
            "dancing_school",
            "driving_school",
            "kindergarten",
            "language_school",
            "music_school",
            "prep_school",
            "school",
            "university",
        ),
        "emergency" to listOf(
            "air_rescue_service",
            "ambulance_station",
            "disaster_response",
            "mountain_rescue",
            "water_rescue",
        ),
        "leisure" to listOf(
            "adult_gaming_centre",
            "amusement_arcade",
            "bowling_alley",
            "dance",
            "escape_game",
            "fitness_centre",
            "hackerspace",
            "ice_rink",
            "indoor_play",
            "sauna",
            "sports_hall",
            "stadium",
            "tanning_salon",
            "trampoline_park",
        ),
        "military" to listOf(
            "office",
        ),
        "tourism" to listOf(
            "alpine_hut",
            "apartment",
            "aquarium",
            "chalet",
            "gallery",
            "guest_house",
            "hostel",
            "hotel",
            "hunting_lodge",
            // "information", only if it is an office, see below
            "museum",
            "motel",
            "trail_riding_station", // motel for riders
            "wilderness_hut"
        ),
        "waterway" to listOf(
            "fuel"
        ),
    )
    .map { it.key + " ~ " + it.value.joinToString("|") }
    .joinToString("\n    or ")

    """
    nodes, ways, relations with
    $tags
    or club
    or craft
    or healthcare
    or office and office !~ no|vacant
    or shop and shop !~ no|vacant
    or tourism = information and information ~ office|visitor_centre
    """.toElementFilterExpression()
}

/** Expression to see if an element is some kind of vacant shop */
private val IS_VACANT_PLACE_EXPRESSION = """
    nodes, ways, relations with
      shop = vacant
      or office = vacant
      or amenity = vacant
""".toElementFilterExpression()

/** iD preset ids of popular place types */
val POPULAR_PLACE_FEATURE_IDS = listOf(
    // ordered roughly by usage number according to taginfo
    "amenity/restaurant", // 1.5 M
    "amenity/cafe", // 0.6 M
    "amenity/fast_food", // 0.6 M

    "shop/convenience", // 0.8 M
    "shop/supermarket", // 0.5 M

    "shop/clothes", // 0.4 M
    "shop/hairdresser", // 0.4 M
    "amenity/pharmacy", // 0.4 M
    "amenity/bank", // 0.4 M

    // More:
    // mostly found only along main roads or certain districts
    //"amenity/fuel", // 0.6 M
    //"tourism/hotel", // 0.4 M
)
