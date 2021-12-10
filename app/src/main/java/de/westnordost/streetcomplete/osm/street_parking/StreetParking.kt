package de.westnordost.streetcomplete.osm.street_parking

import de.westnordost.streetcomplete.osm.street_parking.ParkingOrientation.*
import de.westnordost.streetcomplete.osm.street_parking.ParkingPosition.*

data class LeftAndRightStreetParking(val left: StreetParking?, val right: StreetParking?)

sealed class StreetParking

object StreetParkingProhibited : StreetParking()
object StreetStandingProhibited : StreetParking()
object StreetStoppingProhibited : StreetParking()
object NoStreetParking : StreetParking()
/** When an unknown/unsupported value has been used */
object UnknownStreetParking : StreetParking()
/** When not both parking orientation and position have been specified*/
object IncompleteStreetParking : StreetParking()
/** There is street parking, but it is mapped as separate geometry */
object StreetParkingSeparate : StreetParking()

data class StreetParkingPositionAndOrientation(
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
    PAINTED_AREA_ONLY,
    SHOULDER
}

val StreetParking.estimatedWidthOnRoad: Float get() = when(this) {
    is StreetParkingPositionAndOrientation -> when(orientation) {
        PARALLEL -> 2f * position.estimatedWidthFactor
        DIAGONAL -> 3f * position.estimatedWidthFactor
        PERPENDICULAR -> 4f * position.estimatedWidthFactor
    }
    else -> 0f // otherwise let's assume it's not on the street itself
}

val ParkingPosition.estimatedWidthFactor: Float get() = when(this) {
    ON_STREET -> 1f
    HALF_ON_KERB -> 0.5f
    ON_KERB -> 0f
    else -> 0.5f // otherwise let's assume it is somehow on the street
}

fun ParkingPosition.toOsmValue() = when(this) {
    ON_STREET -> "on_street"
    HALF_ON_KERB -> "half_on_kerb"
    ON_KERB -> "on_kerb"
    STREET_SIDE -> "street_side"
    PAINTED_AREA_ONLY -> "painted_area_only"
    SHOULDER -> "shoulder"
}

fun ParkingOrientation.toOsmValue() = when(this) {
    PARALLEL -> "parallel"
    DIAGONAL -> "diagonal"
    PERPENDICULAR -> "perpendicular"
}
