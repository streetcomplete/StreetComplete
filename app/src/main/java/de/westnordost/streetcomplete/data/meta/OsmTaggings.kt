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

val KEYS_THAT_SHOULD_NOT_BE_REMOVED_WHEN_SHOP_IS_REPLACED = listOf(
    "landuse", "historic", "historic:.*", "heritage", "heritage:.*",
    // building/simple 3d building mapping
    "building", "man_made", "building:.*", "roof:.*", "architect",
    // any address (or weird address: is_in??)
    "addr:.*", "is_in:.*",
    // shop can at the same time be an outline in indoor mapping
    "level", "level:ref", "indoor", "indoor:.*", "room",
    // geometry
    "layer", "ele", "height", "area", "is_in",
    // notes and fixmes
    "FIXME", "fixme", "note",
    // reference tags that are specific to the location and are unlikely to change when a business changes
    // due to the multitude of these, let's keep the whole ref:.* space
    "ref:.*",
    // source tags referring to building / address / geometry (from cursory look at taginfo on "source:")
    "source:(${listOf(
        // referring to address / ref...
        "addr", "address", "housenumber", "street", "conscriptionnumber", "streetnumber", "postcode", "postal_code", "ref",
        // referring to geometry
        "geometry", "position", "shape", "outline", "location",
        // referring to building
        "building", "heritage", "height", "architect", "ele"
    ).joinToString("|")}(:.*)?",
).map { it.toRegex() }
