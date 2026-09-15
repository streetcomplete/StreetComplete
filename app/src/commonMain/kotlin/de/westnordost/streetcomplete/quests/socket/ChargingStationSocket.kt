package de.westnordost.streetcomplete.quests.socket

import kotlinx.serialization.Serializable

/** Sockets used on charging stations for electric vehicles */
@Serializable
enum class ChargingStationSocket(val osmId: String) {
    TYPE1("type1"),
    TYPE1_COMBO("type1_combo"),
    TYPE2_CABLE("type2_cable"),
    TYPE2("type2"),
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

    companion object {
        fun of(osmId: String): ChargingStationSocket? = entries.find { it.osmId == osmId }
    }
}

fun parseChargingStationSockets(tags: Map<String, String>): Map<ChargingStationSocket, Int?> {
    val result = LinkedHashMap<ChargingStationSocket, Int?>()
    for (socket in ChargingStationSocket.entries) {
        val value = tags[socket.osmKey] ?: continue
        val count = value.toIntOrNull()?.takeIf { it >= 0 }
        result[socket] = count ?: if (value == "no") 0 else null
    }
    return result
}
