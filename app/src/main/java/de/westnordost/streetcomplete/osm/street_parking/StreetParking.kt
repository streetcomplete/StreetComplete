package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.Tags
import de.westnordost.streetcomplete.osm.hasCheckDateForKey
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
import de.westnordost.streetcomplete.osm.updateCheckDateForKey
import kotlinx.serialization.Serializable

data class LeftAndRightStreetParking(val left: StreetParking?, val right: StreetParking?)

@Serializable sealed class StreetParking

@Serializable object NoStreetParking : StreetParking()
/** When an unknown/unsupported value has been used */
@Serializable object UnknownStreetParking : StreetParking()
/** When not both parking orientation and position have been specified*/
@Serializable object IncompleteStreetParking : StreetParking()
/** There is street parking, but it is mapped as separate geometry */
@Serializable object StreetParkingSeparate : StreetParking()

@Serializable data class StreetParkingPositionAndOrientation(
    val orientation: ParkingOrientation,
    val position: ParkingPosition
) : StreetParking()

enum class ParkingOrientation {
    PARALLEL, DIAGONAL, PERPENDICULAR
}

enum class ParkingPosition {
    ON_STREET,
    HALF_ON_KERB,
    ON_KERB,
    STREET_SIDE,
    PAINTED_AREA_ONLY
}

fun LeftAndRightStreetParking.validOrNullValues(): LeftAndRightStreetParking {
    if (left?.isValid != false && right?.isValid != false) return this
    return LeftAndRightStreetParking(left?.takeIf { it.isValid }, right?.takeIf { it.isValid })
}

private val StreetParking.isValid: Boolean get() = when(this) {
    IncompleteStreetParking, UnknownStreetParking -> false
    else -> true
}

val StreetParking.estimatedWidthOnRoad: Float get() = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.estimatedWidth * position.estimatedWidthOnRoadFactor
    else -> 0f // otherwise let's assume it's not on the street itself
}

val StreetParking.estimatedWidthOffRoad: Float get() = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.estimatedWidth * (1 - position.estimatedWidthOnRoadFactor)
    else -> 0f // otherwise let's assume it's not on the street itself
}

private val ParkingOrientation.estimatedWidth: Float get() = when (this) {
    PARALLEL -> 2f
    DIAGONAL -> 3f
    PERPENDICULAR -> 4f
}

private val ParkingPosition.estimatedWidthOnRoadFactor: Float get() = when (this) {
    ON_STREET -> 1f
    HALF_ON_KERB -> 0.5f
    ON_KERB -> 0f
    else -> 0.5f // otherwise let's assume it is somehow on the street
}

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
    ON_STREET -> "on_street"
    HALF_ON_KERB -> "half_on_kerb"
    ON_KERB -> "on_kerb"
    STREET_SIDE -> "street_side"
    PAINTED_AREA_ONLY -> "painted_area_only"
}

private fun ParkingOrientation.toOsmValue() = when (this) {
    PARALLEL -> "parallel"
    DIAGONAL -> "diagonal"
    PERPENDICULAR -> "perpendicular"
}
