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

@Serializable object StreetParkingProhibited : StreetParking()
@Serializable object StreetStandingProhibited : StreetParking()
@Serializable object StreetStoppingProhibited : StreetParking()
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

    // was set before and changed: may be incorrect now - remove subtags!
    if (currentParking?.left != null && currentParking.left != left ||
        currentParking?.right != null && currentParking.right != right) {
        /* This includes removing any parking:condition:*, which is a bit peculiar because most
         * values are not even set in this function. But on the other hand, when the physical layout
         * of the parking changes (=redesign of the street layout and furniture), the condition may
         * very well change too, so better delete it to be on the safe side. (It is better to have
         * no data than to have wrong data.) */
        val parkingLaneSubtagging = Regex("^parking:(lane|condition):.*")
        for (key in tags.keys) {
            if (key.matches(parkingLaneSubtagging)) {
                tags.remove(key)
            }
        }
    }

    // parking:lane:<left/right/both>
    val laneRight = right?.toOsmLaneValue()
    val laneLeft = left?.toOsmLaneValue()

    if (laneLeft == laneRight) {
        if (laneLeft != null) tags["parking:lane:both"] = laneLeft
    } else {
        if (laneLeft != null) tags["parking:lane:left"] = laneLeft
        if (laneRight != null) tags["parking:lane:right"] = laneRight
    }

    // parking:condition:<left/right/both>
    val conditionRight = right?.toOsmConditionValue()
    val conditionLeft = left?.toOsmConditionValue()

    if (conditionLeft == conditionRight) {
        if (conditionLeft != null) tags["parking:condition:both"] = conditionLeft
    } else {
        if (conditionLeft != null) tags["parking:condition:left"] = conditionLeft
        if (conditionRight != null) tags["parking:condition:right"] = conditionRight
    }

    // parking:lane:<left/right/both>:<parallel/diagonal/perpendicular>
    val positionRight = (right as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()
    val positionLeft = (left as? StreetParkingPositionAndOrientation)?.position?.toOsmValue()

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
private fun StreetParking.toOsmLaneValue(): String? = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.toOsmValue()
    NoStreetParking, StreetParkingProhibited, StreetStandingProhibited, StreetStoppingProhibited -> "no"
    StreetParkingSeparate -> "separate"
    UnknownStreetParking, IncompleteStreetParking -> null
}

private fun StreetParking.toOsmConditionValue(): String? = when (this) {
    StreetParkingProhibited -> "no_parking"
    StreetStandingProhibited -> "no_standing"
    StreetStoppingProhibited -> "no_stopping"
    else -> null
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
