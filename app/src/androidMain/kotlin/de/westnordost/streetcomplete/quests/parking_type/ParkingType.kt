package de.westnordost.streetcomplete.quests.parking_type

enum class ParkingType(val osmValue: String) {
    SURFACE("surface"),
    STREET_SIDE("street_side"),
    LANE("lane"),
    UNDERGROUND("underground"),
    MULTI_STOREY("multi-storey"),
}
