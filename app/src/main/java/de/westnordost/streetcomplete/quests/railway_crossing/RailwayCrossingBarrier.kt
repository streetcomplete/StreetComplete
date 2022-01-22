package de.westnordost.streetcomplete.quests.railway_crossing

enum class RailwayCrossingBarrier(val osmValue: String?) {
    NO("no"),
    HALF("half"),
    DOUBLE_HALF("double_half"),
    FULL("full"),
    GATE("gate"),
    CHICANE(null) // it's crossing:chicane=yes, not crossing:barrier=chicane
                            // this allows to tag rare cases that have both barrier and chicane
                            // (SC leaves crossing:chicane untagged for crossing with barriers)
}
