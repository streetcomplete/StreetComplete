package de.westnordost.streetcomplete.data.meta

import de.westnordost.streetcomplete.data.elementfilter.toElementFilterExpression

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

val ROADS_ASSUMED_TO_BE_PAVED = arrayOf(
    "trunk", "trunk_link", "motorway", "motorway_link" // see below!
)

val ROADS_TO_ASK_SURFACE_FOR = arrayOf(
// "trunk", "trunk_link", "motorway", "motorway_link", // too much, motorways are almost by definition asphalt (or concrete)
"primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
"unclassified", "residential", "living_street", "pedestrian", "track",
// "service", // this is too much, and the information value is very low
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

val SIDEWALK_SURFACE_KEYS = setOf(
    "sidewalk:both:surface",
    "sidewalk:left:surface",
    "sidewalk:right:surface"
)

const val SURVEY_MARK_KEY = "check_date"
