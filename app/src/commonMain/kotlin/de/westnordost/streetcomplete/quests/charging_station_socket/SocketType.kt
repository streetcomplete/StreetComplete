package de.westnordost.streetcomplete.quests.charging_station_socket

enum class SocketType(val osmKey: String) {
    TYPE2("type2"),
    TYPE2_CABLE("type2_cable"),
    TYPE2_COMBO("type2_combo"),
    CHADEMO("chademo"),
    DOMESTIC("domestic");

    companion object {
        val selectableValues = listOf(
            TYPE2,
            TYPE2_CABLE,
            TYPE2_COMBO,
            CHADEMO,
            DOMESTIC
        )
    }
}
