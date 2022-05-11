package de.westnordost.streetcomplete.quests.crossing_type

enum class CrossingType(val osmValue: String, val raised: Boolean = false) {
    TRAFFIC_SIGNALS("traffic_signals"),
    MARKED("marked"),
    UNMARKED("unmarked"),
    ZEBRA("zebra"),
    UNMARKED_RAISED("unmarked", true),
    MARKED_RAISED("marked", true),
}
