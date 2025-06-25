package de.westnordost.streetcomplete.osm

val MOTORWAYS   = setOf("motorway", "motorway_link")
val TRUNKS      = setOf("trunk", "trunk_link")
val HIGHWAYS    = MOTORWAYS + TRUNKS

val PRIMARY     = setOf("primary", "primary_link")
val SECONDARY   = setOf("secondary", "secondary_link")
val TERTIARY    = setOf("tertiary", "tertiary_link")
val MAJOR_ROADS = PRIMARY + SECONDARY + TERTIARY

val LOCAL_ACCESS_ROADS    = setOf("residential", "living_street")
val PEDESTRIAN_ONLY_ROADS = setOf("pedestrian")

val OTHER_ROADS = setOf("unclassified", "service", "track", "busway", "road")

val ALL_ROADS = HIGHWAYS + MAJOR_ROADS + LOCAL_ACCESS_ROADS +
                PEDESTRIAN_ONLY_ROADS + OTHER_ROADS

val ALL_PATHS = setOf(
    "footway", "cycleway", "path", "bridleway", "steps"
)

val ROADS_ASSUMED_TO_BE_PAVED = HIGHWAYS
