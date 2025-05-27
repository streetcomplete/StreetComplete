package de.westnordost.streetcomplete.osm.building

/** a set of building values that should be treated as deprecated aliases of known building
 *  types */
val INVALID_BUILDING_TYPES = setOf(
    "semi",
    "semidetached",
    "semi_detached",
    "duplex", // -> semidetached_house

    "detached_house", // -> detached

    "terraced_house",
    "terraced",
    "townhouse", // -> terrace

    "mobile_home", // -> static_caravan

    "flats", // -> apartments

    "shop", // -> retail

    "storage_tank", // -> man_made=storage_tank
    "tank", // -> man_made=storage_tank
    "silo", // -> man_made=silo

    "glasshouse", // ambiguous: could be greenhouse or conservatory

    "collapsed",
    "damaged",
    "ruins", // (not explicitly deprecated though)
    "ruin", // -> ruins=yes

    "abandoned",
    "disused", // -> abandoned=yes

    "unclassified",
    "undefined",
    "unknown",
    "other",
    "fixme", // -> yes

    "no",
    "entrance"
)

/** other OSM keys that may describe the purpose of a building */
val OTHER_KEYS_POTENTIALLY_DESCRIBING_BUILDING_TYPE = listOf(
    // See #1854, #1891, #3233
    "man_made",
    "historic",
    "military",
    "power",
    "tourism",
    "attraction",
    "amenity",
    "leisure",
    "aeroway",
    "railway",
    "craft",
    "healthcare",
    "office",
    "shop",
    "description",
    "emergency",
)
