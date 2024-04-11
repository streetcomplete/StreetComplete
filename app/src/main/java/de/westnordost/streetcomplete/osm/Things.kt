package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.mapdata.Element

fun Element.isThing(): Boolean =
    IS_THING_EXPRESSION.matches(this)

fun Element.isDisusedThing(): Boolean =
    this.asIfItWasnt("disused")?.let { IS_THING_EXPRESSION.matches(it) } == true

/** Small map features that are often mapped as points and usually cannot be entered.
 *
 *  To demarcate this category from others, the following are not included:
 *  - structures that are big enough that they would rather be classified alongside buildings
 *    (e.g. water towers)
 *  - permanent features of the landscape (e.g. geysers, peaks, springs)
 *  - things that are features on other map features that wouldn't make sense on their own (e.g.
 *    crossings, traffic lights, entrances, golf holes)
 *  - things that should not be mapped as points - even if allowed - and landuses (e.g. car parks,
 *    dog parks, playgrounds)
 *
 *  Additionally, these things are generally not included:
 *  - things that are intangible / difficult to verify on-site (e.g. fishing spots)
 *  - things that would just be madness to comprehensively map (e.g. manhole covers), maybe what's
 *    madness to map or not is in the eye of the beholder, in this case, in the eyes of app
 *    maintainers
 *
 *  Important: The contribution policy here is that only things may be added if an iD preset for it
 *             exists. This policy exists in order to reduce effort to maintain this list, i.e. we
 *             don't want to check, weigh and balance requests in parallel to iD maintainers (in
 *             terms of notability, it being unambiguous, consensus etc.)
 *  */
private val IS_THING_EXPRESSION by lazy {
    val tags = mapOf(
        "aeroway" to listOf(
            "navigationaid",
            "windsock",
        ),
        "amenity" to listOf(
            // grouped by subcategory, sorted by alphabet

            // transportation
            "bicycle_parking",
            "bicycle_rental",
            "bicycle_repair_station",
            "bicycle_wash",
            "boat_sharing", // like car sharing stations, this is the location of the actual boat(s)
            "car_sharing",
            "charging_station",
            "compressed_air",
            "grit_bin",
            "hitching_post",
            "motorcycle_parking", // a bit like a landuse, but a very small one - similar to bicycle parking
            "taxi",
            "ticket_validator",
            "vacuum_cleaner",

            // financial
            "atm",
            "payment_terminal",

            // healthcare
            "baby_hatch",
            "kneipp_water_cure",

            // entertainment, arts & culture
            "fountain",
            "public_bookcase",
            "whirlpool",

            // public service
            // "post_box", - blocked by https://github.com/streetcomplete/StreetComplete/issues/4916 - which is blocked by https://github.com/westnordost/osmfeatures/issues/23

            // facilities & others
            "baking_oven",
            "bbq",
            "bench",
            "binoculars",
            "clock",
            "device_charging_station",
            "dog_toilet",
            "dressing_room", // though you can enter, it belongs in the same category as toilets
            "drinking_water",
            "food_sharing",
            "give_box",
            "karaoke_box",
            "kitchen", // usually an amenity within campsites etc, i.e. like shower, toilets, ...
            // "letter_box", - see "post_box", but also, it would be very spammy to comprehensively map this
            "library_dropoff",
            "locker",
            "lounger",
            "luggage_locker",
            "mist_spraying_cooler",
            "parcel_locker",
            "photo_booth",
            "security_booth",
            "shelter",
            "shower",
            "smoking_area",
            "stage", // because bandstand is also included, borderline rather a structure
            "telephone",
            "toilets",
            "trolley_bay",
            "vending_machine",
            "water_point",
            "watering_place",

            // waste management
            // "recycling" only for containers, see bottom
            "sanitary_dump_station",
            "waste_basket",
            "waste_disposal",

            // animals
            "feeding_place",
            "game_feeding",
            "hunting_stand",
        ),
        "emergency" to listOf(
            "access_point", // (synonym of highway=emergency_access_point)
            "assembly_point",
            "defibrillator",
            // "emergency_ward_entrance" is rather a type of entrance
            "fire_alarm_box",
            "fire_extinguisher",
            "fire_hose",
            "fire_hydrant",
            "fire_lookout",
            "fire_service_inlet",
            "first_aid_kit",
            "landing_site", // not a helipad but more alike an access_point
            "life_ring",
            "lifeguard",
            "phone",
            "siren",
            // "suction_point" is rather intangible / difficult to verify
            // "water_tank" is more of a structure and would also need to include "fire_water_pond" etc. then
        ),
        "highway" to listOf(
            // "bus_stop", handled in filter below to skip some common bad tagging
            "cyclist_waiting_aid",
            "emergency_access_point",
            "milestone",
            // "speed_camera", - while not directly a sign, it definitely belongs into the traffic
            //                   signals/controls category
            // "speed_display", this is rather like a sign - signs should not go in here
            "street_lamp", // candidate to be moved to lit overlay?
            "trailhead",
            // "traffic_mirror" is rather like a sign - signs should not go in here
        ),
        "historic" to listOf(
            "aircraft",
            "anchor",
            "boundary_stone",
            "cannon",
            "locomotive",
            "milestone",
            "memorial",
            // "monument" - it's rather a structure. Small monuments are tagged as "memorial"
            "railway_car",
            "rune_stone",
            // "ship" - probably too big, more like a structure
            "stone",
            "tank",
            "vehicle",
            // "wreck" - probably too big, and usually quite off-shore anyway
            "wayside_cross",
            "wayside_shrine",
        ),
        "leisure" to listOf(
            "bandstand",
            "bird_hide", // though it is possible to go inside, it's similar to a shelter
            // "bathing_place" - is rather intangible  / difficult to verify,
            //                   also would probably need to include "beach_resort", etc. too
            "firepit",
            // "fishing" is rather intangible / difficult to verify
            "fitness_station",
            "hot_tub",
            "outdoor_seating",
            "slipway",
            "picnic_table",
            "wildlife_hide", // though it is possible to go inside, it's similar to a shelter
        ),
        "man_made" to listOf(
            // larger structures are rather alike buildings, they shouldn't be editable here
            // e.g. "water_tower", "watermill", "windmill", "tower", "telescope", "stupa" ...
            // "antenna" - I think those small-ish antennas for cellular network would be fine
            //             but quite large structures also fall under this tag
            "beehive",
            "cairn",
            "carpet_hanger",
            "column",
            "compass_rose",
            "cross",
            "dolphin", // a bit of a small structure already, but on the other hand, a "boat parking"
            "dovecote",
            "flagpole",
            "insect_hotel",
            // "manhole", - too many of them, it's madness to waste your time mapping these
            "maypole",
            "monitoring_station", // a little large, on the other hand, sizes vary
            "nesting_site",
            // "obelisk" - like historic=monument it's more of a structure
            "planter",
            "snow_cannon",
            "stele",
            // "street_cabinet", - blocked; see note at amenity=post_box, most are included by dedicated filter below
            "surveillance",
            // "survey_point" - this can be very very small -> verifiability issue
            //                  danger that mapper deletes it because he can't find it
            // "telephone_box" - it just describes the structure, but not its use
            // "utility_pole" - usually a vertex
            "video_wall", // basically an advertising=*
            "water_tap",
            "water_well",
        ),
        "natural" to listOf(
            // permanent geology features that are part of the landscape are not included
            // "arch", "cave_entrance", "fumarole", "geyser", "hill", "hot_spring", "peak",
            // "saddle", "spring", "volcano", ...
            "rock", // de-facto synonymous to "stone"
            "stone", // theoretically it can be detached from the rest of the landscape
            "tree",
            "tree_stump",
        ),
        "tourism" to listOf(
            "artwork",
            // "information", only if it is not an office, see below
            "viewpoint",
        )
    )
    .map { it.key + " ~ " + it.value.joinToString("|") }
    .joinToString("\n    or ")

    """
        nodes, ways, relations with
        $tags
        or advertising
        or amenity = recycling and recycling_type = container
        or attraction
        or boundary = marker
        or leisure = pitch and sport ~ chess|table_soccer|table_tennis|teqball
        or man_made = street_cabinet and street_cabinet != postal_service
        or playground
        or public_transport = platform and (
          bus = yes
          or trolleybus = yes
          or tram = yes
        )
        or highway = bus_stop and public_transport != stop_position
        or tourism = information and information !~ office|visitor_centre
    """.toElementFilterExpression()
}

val POPULAR_THING_FEATURE_IDS = listOf(
    "natural/tree/broadleaved",    // 4.0 M
    "highway/street_lamp",         // 4.0 M
    "amenity/bench",               // 2.4 M
    "emergency/fire_hydrant",      // 2.0 M

    "amenity/waste_basket",        // 0.7 M
    "amenity/bicycle_parking",     // 0.6 M
    "amenity/shelter",             // 0.5 M

    "amenity/recycling_container", // 0.4 M
    "amenity/toilets",             // 0.4 M

    // "amenity/post_box",         // 0.4 M
    // blocked by https://github.com/streetcomplete/StreetComplete/issues/4916

    // More:

    // mostly found in parks/plazas, i.e. specific places instead of ~everywhere
    // "historic/memorial",           // 0.4 M (if this is displayed in quick select, artwork should probably too)
    // "amenity/drinking_water",      // 0.3 M
    // "leisure/picnic_table",        // 0.3 M

    // found most often on hiking routes where there are not that many "things" features anyway
    // "information/guidepost",       // 0.5M
    // "tourism/information/board",   // 0.3M
)
