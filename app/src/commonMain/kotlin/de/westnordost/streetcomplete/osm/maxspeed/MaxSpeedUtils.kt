package de.westnordost.streetcomplete.osm.maxspeed

import de.westnordost.streetcomplete.osm.ALL_PATHS

/** OSM keys used to describe implicit speed limits */
val MAX_SPEED_TYPE_KEYS = setOf(
    "source:maxspeed",
    "zone:maxspeed",
    "maxspeed:type",
    "zone:traffic",
    "maxspeed", // in Russia, Serbia, ...
)

/** filter syntax fragment that checks whether the max speed is defined implicitly
 *  (e.g. "source:maxspeed"="DE:urban") */
val FILTER_IS_IMPLICIT_MAX_SPEED =
    "(!maxspeed or ~${MAX_SPEED_TYPE_KEYS.joinToString("|")} ~ \"implicit|([A-Z-]+:.*)\")"

/** filter syntax fragment that checks whether this is a slow zone
 *  (e.g. "source:maxspeed"="DE:zone30") */
val FILTER_IS_IN_SLOW_ZONE =
    "~${MAX_SPEED_TYPE_KEYS.joinToString("|")} ~ \"[A-Z-]+:(zone:?)?([1-9]|[1-2][0-9]|30)\""

/** filter syntax fragment that checks whether it is an implicit max speed but not a slow zone */
val FILTER_IS_IMPLICIT_MAX_SPEED_BUT_NOT_SLOW_ZONE =
    "$FILTER_IS_IMPLICIT_MAX_SPEED and !($FILTER_IS_IN_SLOW_ZONE)"

/**
 * Country subdivisions that each have their own traffic regulations and subsequently may define
 * different default speed limits each. For OpenStreetMap, this means that e.g. a rural default
 * speed limit should be tagged `maxspeed:type=US-WA:rural` instead of `maxspeed:type=US:rural`.
 *
 * For some countries, only certain subdivisions only, e.g. the Netherlands with its oversea
 * territories.
 *
 * Information extracted from https://wiki.openstreetmap.org/wiki/Default_speed_limits
 */
val COUNTRY_SUBDIVISIONS_WITH_OWN_DEFAULT_MAX_SPEEDS: List<Regex> by lazy { listOf(
    "AU-.*",  // ALL states of Australia

    // Belgium:
    "BE-BRU", // Brussels
    "BE-VLG", // Flanders
    "BE-WAL", // Wallonia

    "CA-.*",  // ALL states of Canada

    "CN-HK",  // Hong Kong (aka "HK")
    "CN-MO",  // Macao (aka "MO")
    "CN-TW",  // Taiwan (aka "TW")

    "FM-.*",  // ALL states of Micronesia

    "MD-SN",  // Transnistria

    // Netherlands:
    "NL-AW",           // Aruba (aka "AW")
    "NL-CW",           // Curaçao (aka "CW")
    "NL-SX",           // Sint Maarten (aka "SX")
    "NL-BQ1", "BQ-BO", // Bonaire
    "NL-BQ2", "BQ-SA", // Saba
    "NL-BQ3", "BQ-SE", // Sint Eustatius

    "US-.*",           // ALL states of the United States of America

    "RS-KM",           // Kosovo (aka "XK")
).map { it.toRegex() } }

/** only for roads without a definitive speed limit, it is necessary to determine the "road type"
 *  (e.g. "urban", "rural") to tag the absence of a sign
 */
val ROADS_WITH_DEFINITE_SPEED_LIMIT = setOf("motorway", "living_street")

/** In #5771, #1133, it was determined that actually also main roads may be in a (slow) speed zone
 *  But DEFINITELY not motorways and motorroads! */
val ROADS_WHERE_SLOW_ZONE_IS_NOT_POSSIBLE = setOf("motorway", "trunk")

/** Roads where it is likely enough that they are actually slow zones that the user should be warned
 *  about their existence before providing a "no sign" answer */
val ROADS_WHERE_SLOW_ZONE_IS_LIKELY = setOf("service", "residential", "living_street")

/** highway=living_street is defined as a residential road with the appropriate signage. However,
 *  such a sign could also be posted e.g. on parking lots or footways */
val ROADS_THAT_THAT_MAY_BE_CONVERTED_TO_LIVING_STREET = setOf("residential", "unclassified")

/** Roads for which this option should be shown at all */
val ROADS_THAT_MAY_BE_LIVING_STREETS = setOf(
    "unclassified", "residential", "pedestrian", "living_street", "service") + ALL_PATHS
