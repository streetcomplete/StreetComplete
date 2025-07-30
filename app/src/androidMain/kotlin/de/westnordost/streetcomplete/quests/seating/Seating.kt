package de.westnordost.streetcomplete.quests.seating

enum class Seating(val hasOutdoorSeating: Boolean, val hasIndoorSeating: Boolean) {
    NO(false, false),
    ONLY_INDOOR(false, true),
    ONLY_OUTDOOR(true, false),
    INDOOR_AND_OUTDOOR(true, true),
}
