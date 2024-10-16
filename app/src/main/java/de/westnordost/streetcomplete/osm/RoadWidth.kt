package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.osm.cycleway.estimatedWidth
import de.westnordost.streetcomplete.osm.cycleway.parseCyclewaySides
import de.westnordost.streetcomplete.osm.shoulders.parseShoulders
import de.westnordost.streetcomplete.osm.street_parking.estimatedWidthOffRoad
import de.westnordost.streetcomplete.osm.street_parking.estimatedWidthOnRoad
import de.westnordost.streetcomplete.osm.street_parking.parseStreetParkingSides

/* Functions to estimate road width(s). */

/** Estimated width of the roadway "from curb to curb". So, including any parking
 *  lanes on the street, cycle lanes, shoulders etc.
 *
 *  This function doesn't guess by road type. So it returns null if nothing specified. */
fun estimateRoadwayWidth(tags: Map<String, String>): Float? {
    val roadwayWidth = tags["width:carriageway"]?.toFloatOrNull()
    if (roadwayWidth != null) return roadwayWidth

    val width = tags["width"]?.toFloatOrNull()
    if (width != null) return width

    val lanes = tags["lanes"]?.toIntOrNull()?.coerceAtLeast(1)
    if (lanes != null) {
        val lanesWidth = lanes * when (tags["highway"]) {
            "motorway", "trunk", "motorway_link", "trunk_link" -> BROAD_LANE
            else -> LANE
        }
        val shouldersWidth = (estimateShouldersWidth(tags) ?: 0f)
        return lanesWidth + shouldersWidth
    }

    return null
}

/** Guess width of a roadway. Don't expect any precision from it! */
fun guessRoadwayWidth(tags: Map<String, String>): Float {
    val widthOfOneSide = when (tags["highway"]) {
        "motorway" -> 2 * BROAD_LANE
        "motorway_link" -> BROAD_LANE
        "trunk", "primary" -> BROAD_LANE // to pay respect to that primary roads are usually broader than secondary etc
        "secondary", "tertiary", "unclassified", "busway" -> LANE
        "service" -> 2.5f
        else -> LANE
    }
    return widthOfOneSide * (if (isOneway(tags)) 1f else 2f)
}

/** Estimated width of shoulders, if any. If there is no shoulder tagging, returns null. */
fun estimateShouldersWidth(tags: Map<String, String>): Float? {
    val shoulders = parseShoulders(tags, false) ?: return null
    val shoulderWidth = tags["shoulder:width"]?.toFloatOrNull() ?: SHOULDER
    return (if (shoulders.left) shoulderWidth else 0f) +
        (if (shoulders.right) shoulderWidth else 0f)
}

/** Estimated width of the part of the carriageway that is usable by general traffic, i.e. without
 *  shoulders, parking lanes and cycle lanes.
 *
 *  This function doesn't guess by road type. So it returns null if nothing specified.
 *
 *  However, if no street parking tagging is available or no cycle lanes tagging is available, it
 *  assumes that no street parking and no cycle lanes exist. */
fun estimateUsableRoadwayWidth(tags: Map<String, String>): Float? {
    val width = estimateRoadwayWidth(tags) ?: return null
    return width -
        (estimateParkingOnRoadWidth(tags) ?: 0f) -
        (estimateCycleLanesWidth(tags) ?: 0f) -
        (estimateShouldersWidth(tags) ?: 0f)
}

/** Returns whether the estimated width of the given road is improbable */
fun hasDubiousRoadWidth(tags: Map<String, String>): Boolean? {
    val roadType = tags["highway"]
    if (roadType !in ALL_ROADS) {
        return null
    }

    val usableWidth = estimateUsableRoadwayWidth(tags) ?: return null

    // service roads (alleys, driveways, ...) and tracks don't need to be oneways for it to be
    // plausible to be only as broad as the default profile of OSRM considers as passable by cars
    if (roadType == "service" || roadType == "track") {
        return usableWidth < 1.9f
    }

    // usable width of oneways should be broad enough to accommodate a truck
    if (isOneway(tags)) {
        return usableWidth < 2.6f
    }

    /* one may assume that if the usable width of non-oneway roads is below double the above
       widths, it is also implausible, however, this is actually sometimes the case, by design:
       - on 2-1 roads (roads with no car lanes markings and advisory cycle lanes on both sides)
         https://en.wikipedia.org/wiki/2-1_road
       - certain residential streets with (partial) on-street parking that narrow them down so
         much that drivers have to do a slalom around the parking cars and have to wait on each
         other to pass them
       Hence, to declare such common cases implausible does not make sense.
       However, if the total carriageway (ignoring street parking etc.) of a non-oneway is below
       2x the above, THEN it is dubious
     */
    val width = estimateRoadwayWidth(tags) ?: return null
    return width < 2 * 2.6f
}

/** Estimated width of the street-parking on the roadway.
 *
 *  Returns null if no street parking is specified */
fun estimateParkingOnRoadWidth(tags: Map<String, String>): Float? {
    val sides = parseStreetParkingSides(tags) ?: return null
    return (sides.left?.estimatedWidthOnRoad ?: 0f) + (sides.right?.estimatedWidthOnRoad ?: 0f)
}

/** Estimated width of the street-parking off the roadway
 *
 *  Returns null if no street parking is specified */
fun estimateParkingOffRoadWidth(tags: Map<String, String>): Float? {
    val sides = parseStreetParkingSides(tags) ?: return null
    return (sides.left?.estimatedWidthOffRoad ?: 0f) + (sides.right?.estimatedWidthOffRoad ?: 0f)
}

/** Estimated width the cycle lanes take up space on the roadway.
 *
 *  Returns null if there is no cyclewa tagging at all */
fun estimateCycleLanesWidth(tags: Map<String, String>): Float? =
    estimateCyclewaysWidth(tags, true)

/** Estimated width the cycle lanes take up space from of the roadway
 *
 *  Returns null if there is no cyclewa tagging at all */
fun estimateCycleTrackWidth(tags: Map<String, String>): Float? =
    estimateCyclewaysWidth(tags, false)

private fun estimateCyclewaysWidth(tags: Map<String, String>, isLane: Boolean): Float? {
    val sides = parseCyclewaySides(tags, false) ?: return null

    val leftWidth = if (sides.left?.cycleway?.isLane == isLane) {
        (tags["cycleway:both:width"] ?: tags["cycleway:left:width"])?.toFloatOrNull()
            ?: sides.left.estimatedWidth
    } else {
        0f
    }

    val rightWidth = if (sides.right?.cycleway?.isLane == isLane) {
        (tags["cycleway:both:width"] ?: tags["cycleway:right:width"])?.toFloatOrNull()
            ?: sides.right.estimatedWidth
    } else {
        0f
    }

    return leftWidth + rightWidth
}

// rather under-estimate then over-estimate the road width
private const val BROAD_LANE = 3.75f // standard interstate/motorway width
private const val LANE = 2.75f // widest trucks are about 2.6m
private const val SHOULDER = 2f
