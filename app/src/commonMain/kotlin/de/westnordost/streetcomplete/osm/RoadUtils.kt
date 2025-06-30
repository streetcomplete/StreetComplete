package de.westnordost.streetcomplete.osm

val MOTORWAYS   = setOf("motorway", "motorway_link")
val TRUNKS      = setOf("trunk", "trunk_link")
val HIGHWAYS    = MOTORWAYS + TRUNKS

val PRIMARY     = setOf("primary", "primary_link")
val SECONDARY   = setOf("secondary", "secondary_link")
val TERTIARY    = setOf("tertiary", "tertiary_link")
val MAJOR_ROADS = PRIMARY + SECONDARY + TERTIARY

val LOCAL_ACCESS_ROADS     = setOf("residential", "living_street")
val PEDESTRIAN_ONLY_ROADS  = setOf("pedestrian")
val PUBLIC_TRANSPORT_ROADS = setOf("busway")
val UNCLASSIFIED_ROADS     = setOf("unclassified")

val OTHER_ROADS = setOf("service", "track", "road")

val ALL_MAJOR_AND_HIGHWAYS = HIGHWAYS + MAJOR_ROADS
val ALL_LINKS = ALL_MAJOR_AND_HIGHWAYS.filter { it.endsWith("_link") }.toSet()

val ALL_ROADS = ALL_MAJOR_AND_HIGHWAYS + LOCAL_ACCESS_ROADS +
                PEDESTRIAN_ONLY_ROADS + PUBLIC_TRANSPORT_ROADS +
                UNCLASSIFIED_ROADS + OTHER_ROADS

val PATH_FOR_FOOT_AND_CYCLE = setOf("path", "footway", "cycleway")
val PATH_FOR_EQUESTRIANS    = setOf("bridleway")
val ALL_PATHS = PATH_FOR_FOOT_AND_CYCLE + PATH_FOR_EQUESTRIANS + setOf("steps")

val ALL_PATHS_EXCEPT_STEPS = PATH_FOR_FOOT_AND_CYCLE + PATH_FOR_EQUESTRIANS

val PUBLIC_AND_UNCLASSIFIED =
    PUBLIC_TRANSPORT_ROADS + UNCLASSIFIED_ROADS

val ROADS_WITH_LANES =
    ALL_MAJOR_AND_HIGHWAYS + PUBLIC_AND_UNCLASSIFIED
val ROADS_ASSUMED_TO_BE_PAVED =
    HIGHWAYS
val ROADS_TO_ASK_SMOOTHNESS_FOR =
    ALL_ROADS - MOTORWAYS - OTHER_ROADS + setOf("track")
val TAGGING_NOT_ASSUMED =
    MOTORWAYS + PEDESTRIAN_ONLY_ROADS + PUBLIC_TRANSPORT_ROADS +
        (OTHER_ROADS - setOf("road")) + setOf("living_street")

val LIT_RESIDENTIAL_ROADS =
    PEDESTRIAN_ONLY_ROADS + LOCAL_ACCESS_ROADS + PUBLIC_TRANSPORT_ROADS

val LIT_NON_RESIDENTIAL_ROADS =
    ALL_MAJOR_AND_HIGHWAYS + UNCLASSIFIED_ROADS + setOf("service")

val LIT_WAYS = PATH_FOR_FOOT_AND_CYCLE
