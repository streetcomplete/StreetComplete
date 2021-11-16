package de.westnordost.streetcomplete.quests.railway_crossing

enum class RailwayCrossingBarrier(val osmValue: String?) {
    NO("no"),
    HALF("half"),
    DOUBLE_HALF("double_half"),
    FULL("full"),
    GATE("gate"),
    CHICANE(null) // for some reason, it's crossing:chicane=yes, not crossing:barrier=chicane ¯\_(ツ)_/¯
}
