package de.westnordost.streetcomplete.osm.oneway

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

/** Return whether the given side is in the contra-flow of a oneway. E.g. in Germany for a forward
 *  oneway, it is the left side except of course it goes explicitly in flow direction */
fun isInContraflowOfOneway(tags: Map<String, String>, direction: Direction): Boolean =
    isForwardOneway(tags) && direction == Direction.BACKWARD
    || isReversedOneway(tags) && direction == Direction.FORWARD
