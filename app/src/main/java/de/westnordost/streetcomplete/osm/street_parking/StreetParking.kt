package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.PAINTED_AREA_ONLY
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.STREET_SIDE
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

/** get the OSM value for the parking:lane key */
fun StreetParking.toOsmLaneValue(): String? = when (this) {
    is StreetParkingPositionAndOrientation -> orientation.toOsmValue()
    NoStreetParking, StreetParkingProhibited, StreetStandingProhibited, StreetStoppingProhibited -> "no"
    StreetParkingSeparate -> "separate"
    UnknownStreetParking, IncompleteStreetParking -> null
}

fun StreetParking.toOsmConditionValue(): String? = when (this) {
    StreetParkingProhibited -> "no_parking"
    StreetStandingProhibited -> "no_standing"
    StreetStoppingProhibited -> "no_stopping"
    else -> null
}

fun ParkingPosition.toOsmValue() = when (this) {
    ON_STREET -> "on_street"
    HALF_ON_KERB -> "half_on_kerb"
    ON_KERB -> "on_kerb"
    STREET_SIDE -> "street_side"
    PAINTED_AREA_ONLY -> "painted_area_only"
}

fun ParkingOrientation.toOsmValue() = when (this) {
    PARALLEL -> "parallel"
    DIAGONAL -> "diagonal"
    PERPENDICULAR -> "perpendicular"
}
