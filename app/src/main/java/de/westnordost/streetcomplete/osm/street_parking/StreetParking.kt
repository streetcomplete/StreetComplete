package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.DIAGONAL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PARALLEL
import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.PERPENDICULAR
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.HALF_ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_KERB
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.ON_STREET
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

private val StreetParking.isValid: Boolean get() = when (this) {
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
