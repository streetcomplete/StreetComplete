package de.westnordost.streetcomplete.osm

import de.westnordost.streetcomplete.osm.cycleway.createCyclewaySides
import de.westnordost.streetcomplete.osm.cycleway.estimatedWidth
import de.westnordost.streetcomplete.osm.shoulders.createShoulders
import de.westnordost.streetcomplete.osm.street_parking.createStreetParkingSides
import de.westnordost.streetcomplete.osm.street_parking.estimatedWidthOffRoad
import de.westnordost.streetcomplete.osm.street_parking.estimatedWidthOnRoad

/** Functions to estimate road width(s). */

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
        "motorway", "trunk" -> 2 * BROAD_LANE
        "motorway_link", "trunk_link" -> BROAD_LANE
        "primary" -> BROAD_LANE // to pay respect to that primary roads are usually broader than secondary etc
        "secondary", "tertiary", "unclassified" -> LANE
        "service" -> 2.5f
        else -> LANE
    }
    return widthOfOneSide * (if (isOneway(tags)) 1f else 2f)
}

/** Estimated width of shoulders, if any. If there is no shoulder tagging, returns null. */
fun estimateShouldersWidth(tags: Map<String, String>): Float? {
    val shoulders = createShoulders(tags, false) ?: return null
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

/** Estimated width of the street-parking on the roadway.
 *
 *  Returns null if no street parking is specified */
fun estimateParkingOnRoadWidth(tags: Map<String, String>): Float? {
    val sides = createStreetParkingSides(tags) ?: return null
    return (sides.left?.estimatedWidthOnRoad ?: 0f) + (sides.right?.estimatedWidthOnRoad ?: 0f)
}

/** Estimated width of the street-parking off the roadway
 *
 *  Returns null if no street parking is specified */
fun estimateParkingOffRoadWidth(tags: Map<String, String>): Float? {
    val sides = createStreetParkingSides(tags) ?: return null
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
    val sides = createCyclewaySides(tags, false) ?: return null

    val leftWidth = if (sides.left?.isLane == isLane) {
        (tags["cycleway:both:width"] ?: tags["cycleway:left:width"])?.toFloatOrNull()
            ?: sides.left.estimatedWidth
    } else 0f

    val rightWidth = if (sides.right?.isLane == isLane) {
        (tags["cycleway:both:width"] ?: tags["cycleway:right:width"])?.toFloatOrNull()
            ?: sides.right.estimatedWidth
    } else 0f

    return leftWidth + rightWidth
}

// rather under-estimate then over-estimate the road width
private const val BROAD_LANE = 3.75f // standard interstate/motorway width
private const val LANE = 2.75f // widest trucks are about 2.6m
private const val SHOULDER = 2f
