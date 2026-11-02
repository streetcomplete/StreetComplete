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
    AS3112 -> Res.drawable.socket_domestic_as3112
    BS1363 -> Res.drawable.socket_domestic_bs1363
    CEI23_50_S_11 -> Res.drawable.socket_domestic_cei23_50_s_11
    CEI23_50_S_17 -> Res.drawable.socket_domestic_cei23_50_s_17
    NEMA_1_15 -> Res.drawable.socket_domestic_nema_1_15
    NEMA_14_50 -> Res.drawable.socket_domestic_nema_14_50
    NEMA_5_15 -> Res.drawable.socket_domestic_nema_5_15
    NEMA_5_20 -> Res.drawable.socket_domestic_nema_5_20
    SCHUKO -> Res.drawable.socket_domestic_schuko
    SEV1011_T13 -> Res.drawable.socket_domestic_sev1011_t13
    SEV1011_T15 -> Res.drawable.socket_domestic_sev1011_t15
    SEV1011_T23 -> Res.drawable.socket_domestic_sev1011_t23
    SEV1011_T25 -> Res.drawable.socket_domestic_sev1011_t25
    TYPEA -> Res.drawable.socket_domestic_typea
    TYPEB -> Res.drawable.socket_domestic_typeb
    TYPEC -> Res.drawable.socket_domestic_typec
    TYPED -> Res.drawable.socket_domestic_typed
    TYPEE -> Res.drawable.socket_domestic_typee
    TYPEF -> Res.drawable.socket_domestic_typef
    TYPEG -> Res.drawable.socket_domestic_typeg
    TYPEH -> Res.drawable.socket_domestic_typeh
    TYPEI -> Res.drawable.socket_domestic_typei
    TYPEK -> Res.drawable.socket_domestic_typek
    TYPEL -> Res.drawable.socket_domestic_typel
    TYPEM -> Res.drawable.socket_domestic_typem
    TYPEN -> Res.drawable.socket_domestic_typen
    TYPEO -> Res.drawable.socket_domestic_typeo
    UNIVERSAL_ABCDEFGIO -> Res.drawable.socket_domestic_universal_abcdefgio
    UNIVERSAL_ABCO -> Res.drawable.socket_domestic_universal_abco
    UNIVERSAL_AC -> Res.drawable.socket_domestic_universal_ac
    UNIVERSAL_CI -> Res.drawable.socket_domestic_universal_ci
    UNIVERSAL_DM -> Res.drawable.socket_domestic_universal_dm
    UNIVERSAL_LF -> Res.drawable.socket_domestic_universal_lf
    UNIVERSAL_L -> Res.drawable.socket_domestic_universal_l
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
    AS3112 -> Res.string.quest_charging_station_socket_as3112
    BS1363 -> Res.string.quest_charging_station_socket_bs1363
    CEI23_50_S_11 -> Res.string.quest_charging_station_socket_cei23_50_s_11
    CEI23_50_S_17 -> Res.string.quest_charging_station_socket_cei23_50_s_17
    NEMA_1_15 -> Res.string.quest_charging_station_socket_nema_1_15
    NEMA_14_50 -> Res.string.quest_charging_station_socket_nema_14_50
    NEMA_5_15 -> Res.string.quest_charging_station_socket_nema_5_15
    NEMA_5_20 -> Res.string.quest_charging_station_socket_nema_5_20
    SCHUKO -> Res.string.quest_charging_station_socket_schuko
    SEV1011_T13 -> Res.string.quest_charging_station_socket_sev1011_t13
    SEV1011_T15 -> Res.string.quest_charging_station_socket_sev1011_t15
    SEV1011_T23 -> Res.string.quest_charging_station_socket_sev1011_t23
    SEV1011_T25 -> Res.string.quest_charging_station_socket_sev1011_t25
    TYPEA -> Res.string.quest_charging_station_socket_typea
    TYPEB -> Res.string.quest_charging_station_socket_typeb
    TYPEC -> Res.string.quest_charging_station_socket_typec
    TYPED -> Res.string.quest_charging_station_socket_typed
    TYPEE -> Res.string.quest_charging_station_socket_typee
    TYPEF -> Res.string.quest_charging_station_socket_typef
    TYPEG -> Res.string.quest_charging_station_socket_typeg
    TYPEH -> Res.string.quest_charging_station_socket_typeh
    TYPEI -> Res.string.quest_charging_station_socket_typei
    TYPEK -> Res.string.quest_charging_station_socket_typek
    TYPEL -> Res.string.quest_charging_station_socket_typel
    TYPEM -> Res.string.quest_charging_station_socket_typem
    TYPEN -> Res.string.quest_charging_station_socket_typen
    TYPEO -> Res.string.quest_charging_station_socket_typeo
    UNIVERSAL_ABCDEFGIO -> Res.string.quest_charging_station_socket_universal_abcdefgio
    UNIVERSAL_ABCO -> Res.string.quest_charging_station_socket_universal_abco
    UNIVERSAL_AC -> Res.string.quest_charging_station_socket_universal_ac
    UNIVERSAL_CI -> Res.string.quest_charging_station_socket_universal_ci
    UNIVERSAL_DM -> Res.string.quest_charging_station_socket_universal_dm
    UNIVERSAL_LF -> Res.string.quest_charging_station_socket_universal_lf
    UNIVERSAL_L -> Res.string.quest_charging_station_socket_universal_l
}

val ChargingStationSocket.euLabels: List<Char> get() = when (this) {
    TYPE1 -> listOf('B')
    TYPE2 -> listOf('C')
    TYPE2_CABLE -> listOf('C')
    TYPE2_COMBO -> listOf('K', 'L')
    TYPE3A -> listOf('D')
    TYPE3C -> listOf('E')
    CHADEMO -> listOf('M', 'N')
    else -> emptyList()
}

val ChargingStationSocket.hasCable: Boolean get() = when (this) {
    TYPE2, TYPE3A, TYPE3C, DOMESTIC -> false
    AS3112, BS1363, CEI23_50_S_11, CEI23_50_S_17, NEMA_1_15, NEMA_14_50, NEMA_5_15, NEMA_5_20,
    SCHUKO, SEV1011_T13, SEV1011_T15, SEV1011_T23, SEV1011_T25, TYPEA, TYPEB, TYPEC, TYPED,
    TYPEE, TYPEF, TYPEG, TYPEH, TYPEI, TYPEK, TYPEL, TYPEM, TYPEN, TYPEO,
    UNIVERSAL_ABCDEFGIO, UNIVERSAL_ABCO, UNIVERSAL_AC, UNIVERSAL_CI, UNIVERSAL_DM,
    UNIVERSAL_LF, UNIVERSAL_L -> false
    else -> true
}
