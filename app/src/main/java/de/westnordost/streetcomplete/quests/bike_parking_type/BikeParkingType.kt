package de.westnordost.streetcomplete.quests.bike_parking_type

enum class BikeParkingType(val osmValue: String) {
    STANDS("stands"),
    WALL_LOOPS("wall_loops"),
    SAFE_LOOPS("safe_loops"),
    SHED("shed"),
    LOCKERS("lockers"),
    BUILDING("building"),
    HANDLEBAR_HOLDER("handlebar_holder"),
    SADDLE_HOLDER("saddle_holder"),
    TWO_TIER("two-tier"),
    FLOOR("floor"),
}
