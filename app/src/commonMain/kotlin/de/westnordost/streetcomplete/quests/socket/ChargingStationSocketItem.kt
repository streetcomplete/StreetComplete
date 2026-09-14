package de.westnordost.streetcomplete.quests.socket

import de.westnordost.streetcomplete.quests.socket.ChargingStationSocket.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val ChargingStationSocket.icon: DrawableResource get() = when (this) {
    TYPE1 -> Res.drawable.socket_ev_type1
    TYPE1_COMBO -> Res.drawable.socket_ev_type1_combo
    TYPE2 -> Res.drawable.socket_ev_type2
    TYPE2_CABLE -> Res.drawable.socket_ev_type2_cable
    TYPE2_COMBO -> Res.drawable.socket_ev_type2_combo
    TYPE3A -> Res.drawable.socket_ev_type3a
    TYPE3C -> Res.drawable.socket_ev_type3c
    CHADEMO -> Res.drawable.socket_ev_chademo
    CHAOJI -> Res.drawable.socket_ev_chaoji
    NACS -> Res.drawable.socket_ev_nacs
    GB_AC -> Res.drawable.socket_ev_gb_ac
    GB_DC -> Res.drawable.socket_ev_gb_dc
    DOMESTIC -> Res.drawable.socket_domestic_typec
}

val ChargingStationSocket.title: StringResource get() = when (this) {
    TYPE1 -> Res.string.quest_charging_station_socket_type1
    TYPE1_COMBO -> Res.string.quest_charging_station_socket_type1_combo
    TYPE2 -> Res.string.quest_charging_station_socket_type2
    TYPE2_CABLE -> Res.string.quest_charging_station_socket_type2_cable
    TYPE2_COMBO -> Res.string.quest_charging_station_socket_type2_combo
    TYPE3A -> Res.string.quest_charging_station_socket_type3a
    TYPE3C -> Res.string.quest_charging_station_socket_type3c
    CHADEMO -> Res.string.quest_charging_station_socket_chademo
    CHAOJI -> Res.string.quest_charging_station_socket_chaoji
    NACS -> Res.string.quest_charging_station_socket_nacs
    GB_AC -> Res.string.quest_charging_station_socket_gb_ac
    GB_DC -> Res.string.quest_charging_station_socket_gb_dc
    DOMESTIC -> Res.string.quest_charging_station_socket_domestic
}

val ChargingStationSocket.euLabels: List<Char>? get() = when (this) {
    TYPE1 -> listOf('B')
    TYPE2 -> listOf('C')
    TYPE2_CABLE -> listOf('C')
    TYPE2_COMBO -> listOf('K', 'L')
    TYPE3A -> listOf('D')
    TYPE3C -> listOf('E')
    CHADEMO -> listOf('M', 'N')
    else -> null
}

val ChargingStationSocket.hasCable: Boolean get() = when (this) {
    TYPE2, TYPE3A, TYPE3C -> false
    DOMESTIC -> false
    else -> true
}
