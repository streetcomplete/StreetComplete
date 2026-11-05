package de.westnordost.streetcomplete.quests.socket

import kotlinx.serialization.Serializable

@Serializable
enum class Vehicle
{
    CAR,
    BICYCLE
}

/** Sockets used on charging stations for electric vehicles */
@Serializable
enum class ChargingStationSocket(val osmId: String, val vehicles: List<Vehicle>) {
    TYPE1("type1", listOf(Vehicle.CAR)),
    TYPE1_COMBO("type1_combo", listOf(Vehicle.CAR)),
    TYPE2_CABLE("type2_cable", listOf(Vehicle.CAR)),
    TYPE2("type2", listOf(Vehicle.CAR)),
    TYPE2_COMBO("type2_combo", listOf(Vehicle.CAR)),
    TYPE3A("type3a", listOf(Vehicle.CAR)),
    TYPE3C("type3c", listOf(Vehicle.CAR)),
    CHADEMO("chademo", listOf(Vehicle.CAR)),
    CHAOJI("chaoji", listOf(Vehicle.CAR)),
    NACS("nacs", listOf(Vehicle.CAR)),
    GB_AC("gb_ac", listOf(Vehicle.CAR)),
    GB_DC("gb_dc", listOf(Vehicle.CAR)),
    DOMESTIC("domestic", listOf(Vehicle.CAR)),

    AS3112("as3112", listOf()),
    BS1363("bs1363", listOf()),
    CEI23_50_S_11("cei23_50_s_11", listOf()),
    CEI23_50_S_17("cei23_50_s_17", listOf()),
    NEMA_1_15("nema_1_15", listOf()),
    NEMA_14_50("nema_14_50", listOf()),
    NEMA_5_15("nema_5_15", listOf()),
    NEMA_5_20("nema_5_20", listOf()),
    SCHUKO("schuko", listOf(Vehicle.BICYCLE)),
    SEV1011_T13("sev1011_t13", listOf()),
    SEV1011_T15("sev1011_t15", listOf()),
    SEV1011_T23("sev1011_t23", listOf()),
    SEV1011_T25("sev1011_t25", listOf()),
    TYPEC("typec", listOf(Vehicle.BICYCLE)),
    TYPED("typed", listOf()),
    TYPEE("typee", listOf()),
    TYPEG("typeg", listOf()),
    TYPEH("typeh", listOf()),
    TYPEI("typei", listOf()),
    TYPEK("typek", listOf()),
    TYPEL("typeL", listOf()),
    TYPEM("typem", listOf()),
    TYPEN("typen", listOf()),
    TYPEO("typeo", listOf());

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
