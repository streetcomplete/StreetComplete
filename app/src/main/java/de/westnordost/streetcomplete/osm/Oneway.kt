package de.westnordost.streetcomplete.osm

fun isForwardOneway(tags: Map<String, String>): Boolean =
    tags["oneway"] == "yes" ||
    tags["oneway"] != "-1" && (tags["junction"] == "roundabout" || tags["junction"] == "circular" )

fun isReversedOneway(tags: Map<String, String>): Boolean =
    tags["oneway"] == "-1"

fun isOneway(tags: Map<String, String>): Boolean =
    isForwardOneway(tags) || isReversedOneway(tags)
