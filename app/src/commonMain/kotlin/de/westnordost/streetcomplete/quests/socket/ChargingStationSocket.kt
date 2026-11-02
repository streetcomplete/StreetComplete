package de.westnordost.streetcomplete.quests.socket

import kotlinx.serialization.Serializable

@Serializable
enum class Vehicle()
{
    CAR,
    BICYCLE
}


/** Sockets used on charging stations for electric vehicles */
@Serializable
enum class ChargingStationSocket(val osmId: String, val vehicle: List<Vehicle>) {
    TYPE1("type1", Vehicle.CAR),
    TYPE1_COMBO("type1_combo", Vehicle.CAR),
    TYPE2_CABLE("type2_cable", Vehicle.CAR),
    TYPE2("type2", Vehicle.CAR),
    TYPE2_COMBO("type2_combo", Vehicle.CAR),
    TYPE3A("type3a", Vehicle.CAR),
    TYPE3C("type3c", Vehicle.CAR),
    CHADEMO("chademo", Vehicle.CAR),
    CHAOJI("chaoji", Vehicle.CAR),
    NACS("nacs", Vehicle.CAR),
    GB_AC("gb_ac", Vehicle.CAR),
    GB_DC("gb_dc", Vehicle.CAR),
    DOMESTIC("domestic", Vehicle.CAR),

    AS3112("as3112", Vehicle.BICYCLE),
    BS1363("bs1363", Vehicle.BICYCLE),
    CEI23_50_S_11("cei23_50_s_11", Vehicle.BICYCLE),
    CEI23_50_S_17("cei23_50_s_17", Vehicle.BICYCLE),
    NEMA_1_15("nema_1_15", Vehicle.BICYCLE),
    NEMA_14_50("nema_14_50", Vehicle.BICYCLE),
    NEMA_5_15("nema_5_15", Vehicle.BICYCLE),
    NEMA_5_20("nema_5_20", Vehicle.BICYCLE),
    SCHUKO("schuko", Vehicle.BICYCLE),
    SEV1011_T13("sev1011_t13", Vehicle.BICYCLE),
    SEV1011_T15("sev1011_t15", Vehicle.BICYCLE),
    SEV1011_T23("sev1011_t23", Vehicle.BICYCLE),
    SEV1011_T25("sev1011_t25", Vehicle.BICYCLE),
    TYPEC("typec", Vehicle.BICYCLE),
    TYPED("typed", Vehicle.BICYCLE),
    TYPEE("typee", Vehicle.BICYCLE),
    TYPEG("typeg", Vehicle.BICYCLE),
    TYPEH("typeh", Vehicle.BICYCLE),
    TYPEI("typei", Vehicle.BICYCLE),
    TYPEK("typek", Vehicle.BICYCLE),
    TYPEL("typel", Vehicle.BICYCLE),
    TYPEM("typem", Vehicle.BICYCLE),
    TYPEN("typen", Vehicle.BICYCLE),
    TYPEO("typeo", Vehicle.BICYCLE),
    UNIVERSAL_ABCDEFGIO("universal_abcdefgio", Vehicle.BICYCLE),
    UNIVERSAL_ABCO("universal_abco", Vehicle.BICYCLE),
    UNIVERSAL_AC("universal_ac", Vehicle.BICYCLE),
    UNIVERSAL_CI("universal_ci", Vehicle.BICYCLE),
    UNIVERSAL_DM("universal_dm", Vehicle.BICYCLE),
    UNIVERSAL_LF("universal_lf", Vehicle.BICYCLE),
    UNIVERSAL_L("universal_l", Vehicle.BICYCLE);

    constructor(osmId: String, vehicle: Vehicle) : this(osmId, listOf(vehicle))

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
