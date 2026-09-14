package de.westnordost.streetcomplete.quests.socket

enum class ChargingStationSocket(val osmId: String) {
    TYPE1("type1"),
    TYPE1_COMBO("type1_combo"),
    TYPE2("type2"),
    TYPE2_CABLE("type2_cable"),
    TYPE2_COMBO("type2_combo"),
    TYPE3A("type3a"),
    TYPE3C("type3c"),
    CHADEMO("chademo"),
    CHAOJI("chaoji"),
    NACS("nacs"),
    GB_AC("gb_ac"),
    GB_DC("gb_dc"),
    DOMESTIC("domestic");

    val osmKey: String get() = "socket:$osmId"
}

// TODO   chargingStationSocketTypes.mapNotNull { valueOfOrNull<ChargingStationSocketType>(it) }
