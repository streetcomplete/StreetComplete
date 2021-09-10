package de.westnordost.streetcomplete.data.meta

/** Definitions/meanings of certain OSM taggings  */

val ANYTHING_UNPAVED = setOf(
    "unpaved", "compacted", "gravel", "fine_gravel", "pebblestone", "grass_paver",
    "ground", "earth", "dirt", "grass", "sand", "mud", "ice", "salt", "snow", "woodchips"
)

val ANYTHING_PAVED = setOf(
    "paved", "asphalt", "cobblestone", "cobblestone:flattened", "sett",
    "concrete", "concrete:lanes", "concrete:plates", "paving_stones",
    "metal", "wood", "unhewn_cobblestone"
)
val ALL_ROADS = setOf(
    "motorway", "motorway_link", "trunk", "trunk_link", "primary", "primary_link",
    "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian",
    "service", "track", "road"
)

val ALL_PATHS = setOf(
    "footway", "cycleway", "path", "bridleway", "steps"
)

val MAXSPEED_TYPE_KEYS = setOf(
    "source:maxspeed",
    "zone:maxspeed",
    "maxspeed:type",
    "zone:traffic"
)

const val SURVEY_MARK_KEY = "check_date"

val KEYS_THAT_SHOULD_BE_REMOVED_WHEN_SHOP_IS_REPLACED = listOf(
    "shop", "craft", "amenity", "shop_1", "old_shop", "craft_1",
    // obsoleted information
    "disused:.*", "was:.*", "damage", "source:damage", "created_by", "check_date",
    // classifications / links to external databases
    "fhrs:.*", "fvst:.*", "ncat", "nat_ref", "gnis:.*", "winkelnummer",
    // names and identifications
    "name(:.*)?", "name_1", ".*_name(:.*)?", "noname", "branch(:.*)?", "brand(:.*)?", "network", "operator(:.*)?", "ref", "ref:vatin", "designation",
    // contacts
    "contact:.*", "website", "facebook", "email", "phone(:.*)?", "phone_1", "mobile", "fax", "url", "source_ref:url", "owner",
    // payments
    "payment:.*", "currency:.*", "cash_withdrawal(:.*)?", "fee",
    // generic shop/craft attributes
    "opening_hours(:.*)?", "check_date:opening_hours", "internet_access(:.*)?", "second_hand", "self_service", "license:.*", "bulk_purchase", ".*:covid19", "description(:.*)?",
    "min_age", "supermarket(:.*)?", "operational_status", "trade", "wholesale", "sale", "smoking", "origin", "attraction", "strapline", "dog",
    // food and drink details
    "bar", "cafe", "microbrewery", "brewery", "real_ale", "distillery", "drink:.*", "cocktails", "alcohol", "wine:region", "wine_region", "diet:.*", "cuisine",
    "organic", "produced_on_site", "food", "pastry", "product", "produce", "chocolate", "fair_trade", "butcher", "reservation", "takeaway(:.*)?", "delivery", "caterer",
    // fuel stations and boatbuilders
    "fuel:.*", "compressed_air", "hgv:lanes", "seamark:.*", "waterway",
    // related to repair shops/crafts
    "service(:.*)?", "motorcycle:.*", "repair", ".*:repair", "electronics_repair(:.*)?",
    // hairdresser
    "unisex", "male", "female",
    // accomodation & layout
    "rooms", "stars", "accommodation", "beds", "capacity(:persons)?", "tourism", "laundry_service",
    // misc specific attributes
    "clothes", "beauty", "tobacco", "carpenter", "furniture", "lottery", "sport", "leisure", "dispensing", "tailor:.*",
    "books", "agrarian", "musical_instrument", "massage", "parts", "post_office(:.*)?", "religion", "rental", "tickets:.*", "goods_supply",
).map { it.toRegex() }

/** ~ tenant of a normal retail shop area.
 *  So,
 *  - no larger or purpose-built things like malls, cinemas, theatres, car washes, fuel stations,
 *    museums, galleries, zoos, aquariums, bowling alleys...
 *  - no things that are usually not found in normal retail shop areas but in offices:
 *    clinics, doctors, fitness centers, dental technicians...
 *  - nothing that is rather located in an industrial estate like car repair and other types
 *    of workshops (most craft=* other than those where people go to have something repaired or so)
 *
 *  It is possible to specify a prefix for the keys here, e.g. "disused", to find disused shops etc.
 *  */
fun isKindOfShopExpression(prefix: String? = null): String {
    val p = if(prefix != null) "$prefix:" else ""
    return ("""
        ${p}shop and ${p}shop !~ no|vacant|mall
        or ${p}tourism = information and ${p}information = office
        or """ +
        mapOf(
            "amenity" to arrayOf(
                "restaurant", "cafe", "ice_cream", "fast_food", "bar", "pub", "biergarten", "nightclub",
                "bank", "bureau_de_change", "money_transfer", "post_office", "internet_cafe",
                "pharmacy",
                "driving_school",
            ),
            "leisure" to arrayOf(
                "amusement_arcade", "adult_gaming_centre", "tanning_salon",
            ),
            "office" to arrayOf(
                "insurance", "travel_agent", "tax_advisor", "estate_agent", "political_party",
            ),
            "craft" to arrayOf(
                "shoemaker", "tailor", "photographer", "watchmaker", "optician",
                "electronics_repair", "key_cutter",
            )
        ).map { p + it.key + " ~ " + it.value.joinToString("|") }.joinToString("\n  or ") + "\n"
        ).trimIndent()
}
