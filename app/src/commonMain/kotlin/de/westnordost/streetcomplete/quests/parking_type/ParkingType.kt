package de.westnordost.streetcomplete.quests.parking_type

import kotlinx.serialization.Serializable

@Serializable
enum class ParkingType(val osmValue: String) {
    SURFACE("surface"),
    STREET_SIDE("street_side"),
    LANE("lane"),
    UNDERGROUND("underground"),
    MULTI_STOREY("multi-storey"),
}
