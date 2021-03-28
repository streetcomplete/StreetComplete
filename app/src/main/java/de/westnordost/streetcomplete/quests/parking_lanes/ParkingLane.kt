package de.westnordost.streetcomplete.quests.parking_lanes

sealed class ParkingLane

data class ParallelParkingLane(val position: ParkingLanePosition?) : ParkingLane()
data class DiagonalParkingLane(val position: ParkingLanePosition?) : ParkingLane()
data class PerpendicularParkingLane(val position: ParkingLanePosition?) : ParkingLane()
object MarkedParkingLane: ParkingLane()
object NoParking : ParkingLane()
object NoStopping : ParkingLane()
object FireLane: ParkingLane()
object NoParkingLane : ParkingLane()
object UnknownParkingLane : ParkingLane()

enum class ParkingLanePosition {
    ON_STREET,
    HALF_ON_KERB,
    ON_KERB,
    LAY_BY,
    PAINTED_AREA_ONLY,
    SHOULDER,
    UNKNOWN
}

val ParkingLane.estimatedWidthOnRoad: Float get() = when(this) {
    is ParallelParkingLane -> 2f * (position?.estimatedWidthFactor ?: 1f)
    is DiagonalParkingLane -> 3f * (position?.estimatedWidthFactor ?: 1f)
    is PerpendicularParkingLane -> 4f * (position?.estimatedWidthFactor ?: 1f)
    else -> 0f // otherwise let's assume it's not on the street itself
}

val ParkingLanePosition.estimatedWidthFactor: Float get() = when(this) {
    ParkingLanePosition.ON_STREET -> 1f
    ParkingLanePosition.HALF_ON_KERB -> 0.5f
    ParkingLanePosition.ON_KERB -> 0f
    else -> 0.5f // otherwise let's assume it is somehow on the street
}
