package de.westnordost.streetcomplete.osm

val ALL_ROADS = setOf(
    "motorway", "motorway_link", "trunk", "trunk_link",
    "primary", "primary_link", "secondary", "secondary_link", "tertiary", "tertiary_link",
    "unclassified", "residential", "living_street", "pedestrian",
    "service", "track", "busway", "road",
)

val ALL_PATHS = setOf(
    "footway", "cycleway", "path", "bridleway", "steps"
)

val ROADS_ASSUMED_TO_BE_PAVED = arrayOf(
    "trunk", "trunk_link", "motorway", "motorway_link"
)
