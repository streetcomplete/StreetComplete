package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression
import de.westnordost.streetcomplete.data.osm.edits.update_tags.StringMapChangesBuilder
import de.westnordost.streetcomplete.data.osm.mapdata.Element

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

/** Get tags to denote the element with the given [tags] as disused */
fun getDisusedPlaceTags(tags: Map<String, String>?): Map<String, String> {
    val (key, value) = tags?.entries?.find { it.key in placeTypeKeys }?.toPair() ?: ("shop" to "yes")
    return mapOf("disused:$key" to value)
}

private val placeTypeKeys = setOf(
    "amenity",
    "club",
    "craft",
    "emergency",
    "healthcare",
    "leisure",
    "office",
    "military",
    "shop",
    "tourism"
)

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
    "amenity/restaurant", // 1.4 M
    "amenity/cafe", // 0.5 M
    "amenity/fast_food", // 0.5 M
    "shop/convenience", // 0.7 M
    "shop/supermarket", // 0.5 M
    "shop/clothes", // 0.4 M
    "shop/hairdresser", // 0.3 M
    "tourism/hotel", // 0.4 M
    "amenity/pharmacy", // 0.3 M
)

/** Replace a place with the given new tags.
 *  Removes any place-related tags before adding the given [tags]. */
fun StringMapChangesBuilder.replacePlace(tags: Map<String, String>) {
    removeCheckDates()

    for (key in keys) {
        if (KEYS_THAT_SHOULD_BE_REMOVED_WHEN_PLACE_IS_REPLACED.any { it.matches(key) }) {
            remove(key)
        }
    }

    for ((key, value) in tags) {
        this[key] = value
    }
}

// generated by "make update" from https://github.com/mnalis/StreetComplete-taginfo-categorize/
private val KEYS_THAT_SHOULD_BE_REMOVED_WHEN_PLACE_IS_REPLACED = listOf(
    "shop_?[1-9]?(:.*)?", "craft_?[1-9]?", "amenity_?[1-9]?", "club_?[1-9]?", "old_amenity",
    "old_shop", "information", "leisure", "office_?[1-9]?", "tourism",
    // popular shop=* / craft=* subkeys
    "marketplace", "household", "swimming_pool", "laundry", "golf", "sports", "ice_cream",
    "scooter", "music", "retail", "yes", "ticket", "newsagent", "lighting", "truck", "car_repair",
    "car_parts", "video", "fuel", "farm", "car", "tractor", "hgv", "ski", "sculptor",
    "hearing_aids", "surf", "photo", "boat", "gas", "kitchen", "anime", "builder", "hairdresser",
    "security", "bakery", "bakehouse", "fishing", "doors", "kiosk", "market", "bathroom", "lamps",
    "vacant", "insurance(:.*)?", "caravan", "gift", "bicycle", "bicycle_rental", "insulation",
    "communication", "mall", "model", "empty", "wood", "hunting", "motorcycle", "trailer",
    "camera", "water", "fireplace", "outdoor", "blacksmith", "electronics", "fan", "piercing",
    // obsoleted information
    "abandoned(:.*)?", "disused(:.*)?", "was:.*", "not:.*", "damage", "source:damage",
    "created_by", "check_date", "opening_date", "last_checked", "checked_exists:date",
    "pharmacy_survey", "old_ref", "update", "import_uuid", "review", "fixme:atp",
    // classifications / links to external databases
    "fhrs:.*", "old_fhrs:.*", "fvst:.*", "ncat", "nat_ref", "gnis:.*", "winkelnummer",
    "type:FR:FINESS", "type:FR:APE", "kvl_hro:amenity", "ref:DK:cvr(:.*)?", "certifications?",
    "transiscope", "opendata:type", "local_ref", "official_ref",
    // names and identifications
    "name_?[1-9]?(:.*)?", ".*_name_?[1-9]?(:.*)?", "noname", "branch(:.*)?", "brand(:.*)?",
    "not:brand(:.*)?", "network(:wikidata)?", "operator(:.*)?", "operator_type", "ref",
    "ref:vatin", "designation", "SEP:CLAVEESC", "identifier", "ref:FR:SIRET", "ref:FR:SIREN",
    "ref:FR:NAF",
    // contacts
    "contact_person", "contact(:.*)?", "phone(:.*)?", "phone_?[1-9]?", "emergency:phone",
    "emergency_telephone_code",
    "mobile", "fax", "facebook", "instagram", "twitter", "youtube", "telegram", "email",
    "website_?[1-9]?(:.*)?", "app:.*", "ownership",
    "url", "url:official", "source_ref:url", "owner",
    // payments
    "payment(:.*)?", "payment_multi_fee", "currency:.*", "check_date:currency:.*",
    "cash_withdrawal(:.*)?", "fee", "charge", "charge_fee", "money_transfer",
    "donation:compensation",
    // generic shop/craft attributes
    "seasonal", "time", "opening_hours(:.*)?", "check_date:opening_hours", "check_(in|out)",
    "wifi", "internet", "internet_access(:.*)?", "second_hand", "self_service", "automated",
    "license:.*", "bulk_purchase", ".*:covid19", "language:.*", "baby_feeding",
    "description(:.*)?", "description[0-9]", "min_age", "max_age", "supermarket(:.*)?",
    "social_facility(:.*)?", "functional", "trade", "wholesale", "sale", "smoking(:outside)?",
    "zero_waste", "origin", "attraction", "strapline", "dog", "showroom", "toilets?(:.*)?",
    "sanitary_dump_station", "changing_table(:.*)?", "wheelchair(.*)?", "blind", "company(:.*)?",
    "stroller", "walk-in", "webshop", "operational_status.*", "status", "drive_through",
    "surveillance(:.*)?", "outdoor_seating", "indoor_seating", "colour", "access_simple", "floor",
    "product_category", "source_url", "category", "kids_area", "kids_area:indoor", "resort",
    "since", "state", "temporary", "self_checkout", "audio_loop",
    // food and drink details
    "bar", "cafe", "coffee", "microroasting", "microbrewery", "brewery", "real_ale", "taproom",
    "training", "distillery", "drink(:.*)?", "cocktails", "alcohol", "wine([:_].*)?",
    "happy_hours", "diet:.*", "cuisine", "ethnic", "tasting", "breakfast", "lunch", "organic",
    "produced_on_site", "restaurant", "food", "pastry", "pastry_shop", "product", "produce",
    "chocolate", "fair_trade", "butcher", "reservation(:.*)?", "takeaway(:.*)?", "delivery(:.*)?",
    "caterer", "real_fire", "flour_fortified", "highchair", "fast_food", "pub", "snack",
    "confectionery",
    // related to repair shops/crafts
    "service(:.*)?", "motorcycle:.*", "repair", ".*:repair", "electronics_repair(:.*)?",
    "workshop",
    // shop=hairdresser, shop=clothes
    "unisex", "male", "female", "gender", "gender_simple", "lgbtq(:.*)?", "gay", "female:signed",
    "male:signed",
    // healthcare
    "healthcare(:.*)?", "healthcare_.*", "health", "health_.*", "medical_.*", "facility(:.*)?",
    "activities", "healthcare_facility(:.*)?", "laboratory(:.*)?", "blood(:.*)?",
    "blood_components", "infection(:.*)?", "disease(:.*)?", "covid19(:.*)?", "COVID_.*",
    "CovidVaccineCenterId", "coronaquarantine", "hospital(:.*)?", "hospital_type_id",
    "emergency_room", "sample_collection(:.*)?", "bed_count", "capacity:beds", "part_time_beds",
    "personnel:count", "staff_count(:.*)?", "admin_staff", "doctors", "doctors_num", "nurses_num",
    "counselling_type", "testing_centres", "toilets_number", "urgent_care", "vaccination",
    "clinic", "hospital", "pharmacy", "alternative", "laboratory", "sample_collection",
    "provided_for(:.*)?", "social_facility_for", "ambulance", "ward",
    "HSE_(code|hgid|hgroup|region)", "collection_centre", "design", "AUTORIZATIE", "reg_id",
    "post_addr", "scope", "ESTADO", "NIVSOCIO", "NO", "EMP_EST", "COD_HAB", "CLA_PERS", "CLA_PRES",
    "snis_code:.*", "hfac_bed", "hfac_type", "nature", "moph_code", "IJSN:.*", "massgis:id",
    "OGD-Stmk:.*", "paho:.*", "panchayath", "pbf_contract", "pcode", "pe:minsa:.*", "who:.*",
    "pharmacy:category", "tactile_paving", "HF_(ID|TYPE|N_EN)", "RoadConn", "bin",
    // accommodation & layout
    "rooms", "stars", "accommodation", "beds", "capacity(:persons)?", "laundry_service",
    "guest_house",
    // amenity=place_of_worship
    "deanery", "subject:(wikidata|wikipedia)",
    // schools
    "capacity:(pupils|teachers)", "grades", "population:pupils:.*",
    "school:(FR|gender|trust|type|type_idn)", "primary",
    // clubs
    "animal(_breeding|_training)?", "billiards(:.*)?", "board_game", "sport_1", "sport:boating",
    "boat:type", "canoe(_rental|:service)?", "kayak(_rental|:service)?",
    "sailboat(_rental|:service)?", "horse_riding", "rugby", "boules", "callsign", "card_games",
    "car_service", "catastro:ref", "chess(:.*)?", "children", "climbing(:.*)?", "club(:.*)?",
    "communication(:amateur_radio.*)", "community_centre:for", "dffr:network", "dormitory",
    "education_for:ages", "electrified", "esperanto", "events_venue", "family", "federation",
    "free_flying(:.*)?", "freemasonry(:.*)?", "free_refill", "gaelic_games(:.*)?", "membership",
    "military_service", "model_aerodrome(:.*)?", "mode_of_organisation(:.*)?", "snowmobile",
    "social_centre(:for)?", "source_dat", "tennis", "old_website", "organisation", "school_type",
    "scout(:type)?", "fraternity", "live_music", "lockable", "playground(:theme)?", "nudism",
    "music_genre", "length", "fire_station:type:FR", "cadet", "observatory:type", "tower:type",
    "zoo", "shooting", "commons", "groomer", "group_only", "hazard", "identity", "interaction",
    "logo", "maxheight", "provides", "regional", "scale", "site", "plots", "allotments",
    "local_food", "monitoring:pedestrian", "recording:automated",
    // misc specific attributes
    "clothes", "shoes", "tailor", "beauty", "tobacco", "carpenter", "furniture", "lottery",
    "sport", "dispensing", "tailor:.*", "gambling", "material", "raw_material", "stonemason",
    "studio", "scuba_diving(:.*)?", "polling_station", "collector", "books", "agrarian",
    "musical_instrument", "massage", "parts", "post_office(:.*)?", "religion", "denomination",
    "rental", ".*:rental", "tickets:.*", "public_transport", "goods_supply", "pet", "appliance",
    "artwork_type", "charity", "company", "crop", "dry_cleaning", "factory", "feature",
    "air_conditioning", "atm", "vending", "vending_machine", "recycling_type", "museum",
    "license_classes", "dance:.*", "isced:level", "school", "preschool", "university",
    "research_institution", "research", "member_of", "topic", "townhall:type", "parish", "police",
    "government", "office", "administration", "administrative", "association", "transport",
    "utility", "consulting", "Commercial", "commercial", "private", "taxi", "admin_level",
    "official_status", "target", "liaison", "diplomatic(:.*)?", "embassy", "consulate", "aeroway",
    "department", "faculty", "aerospace:product", "boundary", "population", "diocese", "depot",
    "cargo", "function", "game", "party", "political_party.*", "telecom(munication)?",
    "service_times", "kitchen:facilities", "it:(type|sales)", "cannabis:cbd", "bath:type",
    "bath:(open_air|sand_bath)", "animal_boarding", "animal_shelter", "mattress", "screen",
    "monitoring:weather", "public", "theatre", "culture", "library", "cooperative",
    "camp_site", "camping", "emergency(:.*)?", "evacuation_cent(er|re)", "education",
    "engineering", "forestry", "foundation", "lawyer", "logistics", "military", "community_centre",
    "bank", "operational",
    "Comments?", "comments?", "entrance:(width|step_count|kerb:height)", "fenced", "motor_vehicle",
).map { it.toRegex() }
