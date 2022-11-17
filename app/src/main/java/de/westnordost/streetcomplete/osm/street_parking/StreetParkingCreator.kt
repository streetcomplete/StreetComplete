package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.updateCheckDateForKey

fun LeftAndRightStreetParking.applyTo(tags: Tags) {
    val currentParking = createStreetParkingSides(tags)

    // first clear previous
    val keyToRemove = Regex("parking:lane:(both|left|right)(:(parallel|diagonal|perpendicular))?")
    for (key in tags.keys) {
        if (key.matches(keyToRemove)) tags.remove(key)
    }

    val r = right ?: currentParking?.right
    val l = left ?: currentParking?.left

    // parking:lane:<left/right/both>
    val laneRight = r?.toOsmLaneValue()
    val laneLeft =  l?.toOsmLaneValue()

    if (laneLeft == laneRight) {
        if (laneLeft != null) tags["parking:lane:both"] = laneLeft
    } else {
        if (laneLeft != null) tags["parking:lane:left"] = laneLeft
        if (laneRight != null) tags["parking:lane:right"] = laneRight
    }

    // parking:lane:<left/right/both>:<parallel/diagonal/perpendicular>
    val positionRight = (r as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()
    val positionLeft = (l as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()

    if (laneLeft == laneRight && positionLeft == positionRight) {
        if (positionLeft != null) tags["parking:lane:both:$laneLeft"] = positionLeft
    } else {
        if (positionLeft != null) tags["parking:lane:left:$laneLeft"] = positionLeft
        if (positionRight != null) tags["parking:lane:right:$laneRight"] = positionRight
    }

    if (!tags.hasChanges || tags.hasCheckDateForKey("parking:lane")) {
        tags.updateCheckDateForKey("parking:lane")
    }
}

/** get the OSM value for the parking:lane key */
private fun StreetParking.toOsmLaneValue(): String = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.toOsmValue()
    NoStreetParking -> "no"
    StreetParkingSeparate -> "separate"
    UnknownStreetParking, IncompleteStreetParking -> throw IllegalArgumentException("Attempting to tag invalid parking lane")
}

private fun ParkingPosition.toOsmValue() = when (this) {
    ParkingPosition.ON_STREET -> "on_street"
    ParkingPosition.HALF_ON_KERB -> "half_on_kerb"
    ParkingPosition.ON_KERB -> "on_kerb"
    ParkingPosition.STREET_SIDE -> "street_side"
    ParkingPosition.PAINTED_AREA_ONLY -> "painted_area_only"
}

private fun ParkingOrientation.toOsmValue() = when (this) {
    ParkingOrientation.PARALLEL -> "parallel"
    ParkingOrientation.DIAGONAL -> "diagonal"
    ParkingOrientation.PERPENDICULAR -> "perpendicular"
}
