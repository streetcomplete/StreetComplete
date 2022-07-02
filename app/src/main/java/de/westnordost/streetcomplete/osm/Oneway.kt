package de.westnordost.streetcomplete.osm

fun isForwardOneway(tags: Map<String, String>): Boolean =
    tags["oneway"] == "yes"
    || tags["oneway"] != "-1" && (tags["junction"] == "roundabout" || tags["junction"] == "circular")

fun isReversedOneway(tags: Map<String, String>): Boolean =
    tags["oneway"] == "-1"

fun isOneway(tags: Map<String, String>): Boolean =
    isForwardOneway(tags) || isReversedOneway(tags)

fun isNotOnewayForCyclists(tags: Map<String, String>, isLeftHandTraffic: Boolean): Boolean =
    tags["oneway:bicycle"] == "no"
    || tags["cycleway"]?.startsWith("opposite") == true
    // any unambiguous opposite tagging implies oneway:bicycle = no
    || tags[
        if (isLeftHandTraffic xor isReversedOneway(tags)) "cycleway:right" else "cycleway:left"
    ]?.startsWith("opposite") == true
