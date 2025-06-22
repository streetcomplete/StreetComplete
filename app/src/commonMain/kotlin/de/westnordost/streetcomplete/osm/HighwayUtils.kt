package de.westnordost.streetcomplete.osm

val MOTORWAYS = setOf("motorway", "motorway_link")
val TRUNKS    = setOf("trunk", "trunk_link")
val PRIMARY   = setOf("primary", "primary_link")
val SECONDARY = setOf("secondary", "secondary_link")
val TERTIARY  = setOf("tertiary", "tertiary_link")

val CLASSIFIED_ROADS = PRIMARY + SECONDARY + TERTIARY
val PEDESTRIAN_ROADS = setOf("residential", "living_street", "pedestrian")
val OTHER_ROADS      = setOf("unclassified", "service", "track", "busway", "road")

val ALL_ROADS = MOTORWAYS + TRUNKS + CLASSIFIED_ROADS +
                PEDESTRIAN_ROADS + OTHER_ROADS

val ALL_PATHS = setOf(
    "footway", "cycleway", "path", "bridleway", "steps"
)

val ROADS_ASSUMED_TO_BE_PAVED = arrayOf(
    "trunk", "trunk_link", "motorway", "motorway_link"
)
