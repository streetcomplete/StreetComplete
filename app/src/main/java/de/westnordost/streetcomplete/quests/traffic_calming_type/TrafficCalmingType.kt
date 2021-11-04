package de.westnordost.streetcomplete.quests.traffic_calming_type

enum class TrafficCalmingType(val osmValue: String) {
    BUMP("bump"),
    HUMP("hump"),
    TABLE("table"),
    CUSHION("cushion"),
    ISLAND("island"),
    CHOKER("choker"),
    CHICANE("chicane"),
    RUMBLE_STRIP("rumble_strip"),
}
