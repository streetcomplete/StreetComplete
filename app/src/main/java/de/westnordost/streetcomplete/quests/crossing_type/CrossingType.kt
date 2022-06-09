package de.westnordost.streetcomplete.quests.crossing_type

enum class CrossingType(val osmValue: String, var raised: Boolean = false, val zebra: Boolean = false) {
    TRAFFIC_SIGNALS("traffic_signals"),
    TRAFFIC_SIGNALS_ZEBRA("traffic_signals", zebra = true),
    MARKED("marked"),
    ZEBRA("marked", zebra = true),
    UNMARKED("unmarked"),
    RAISED(""),
}
