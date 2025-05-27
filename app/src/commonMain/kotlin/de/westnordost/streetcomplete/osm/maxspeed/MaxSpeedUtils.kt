package de.westnordost.streetcomplete.osm.maxspeed

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
