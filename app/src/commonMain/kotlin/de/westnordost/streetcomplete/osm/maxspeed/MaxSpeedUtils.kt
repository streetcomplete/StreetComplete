package de.westnordost.streetcomplete.osm.maxspeed

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
