package de.westnordost.streetcomplete.quests.railway_electrification

enum class RailwayElectrificationSystem( val osmValue: String ) {
    NO("no"),
    CONTACT_LINE("contact_line"),
    THIRD_RAIL("rail"),
    FOURTH_RAIL("4th_rail"),
    GROUND_LEVEL("ground_level_power_supply"),
}
