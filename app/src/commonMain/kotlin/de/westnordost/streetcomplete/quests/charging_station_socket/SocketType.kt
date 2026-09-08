package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class SocketType(val osmKey: String) {
    TYPE2("type2"),
    TYPE2_CABLE("type2_cable"),
    TYPE2_COMBO("type2_combo"),
    CHADEMO("chademo"),
    DOMESTIC("domestic");

    val osmCountKey: String get() = "socket:$osmKey"
}

val SocketType.icon: DrawableResource
    get() = when (this) {
        SocketType.TYPE2 -> Res.drawable.socket_type2
        SocketType.TYPE2_CABLE -> Res.drawable.socket_type2_cable
        SocketType.TYPE2_COMBO -> Res.drawable.socket_type2_combo
        SocketType.CHADEMO -> Res.drawable.socket_chademo
        SocketType.DOMESTIC -> Res.drawable.socket_domestic
    }

val SocketType.euLabels: List<DrawableResource>
    get() = when (this) {
        SocketType.TYPE2 -> listOf(Res.drawable.socket_eu_c_white)
        SocketType.TYPE2_CABLE -> listOf(Res.drawable.socket_eu_c_black)
        SocketType.TYPE2_COMBO -> listOf(
            Res.drawable.socket_eu_k_black,
            Res.drawable.socket_eu_l_black
        )
        SocketType.CHADEMO -> listOf(
            Res.drawable.socket_eu_m_black,
            Res.drawable.socket_eu_n_black
        )
        SocketType.DOMESTIC -> emptyList()
    }

val SocketType.title: StringResource
    get() = when (this) {
        SocketType.TYPE2 -> Res.string.quest_charging_station_socket_type2
        SocketType.TYPE2_CABLE -> Res.string.quest_charging_station_socket_type2_cable
        SocketType.TYPE2_COMBO -> Res.string.quest_charging_station_socket_type2_combo
        SocketType.CHADEMO -> Res.string.quest_charging_station_socket_chademo
        SocketType.DOMESTIC -> Res.string.quest_charging_station_socket_domestic
    }

/** Official black EU identifier artwork that must not be theme-tinted. */
val SocketType.hasBlackEuLabels: Boolean
    get() = this != SocketType.TYPE2 && euLabels.isNotEmpty()

/** Supported non-DOMESTIC socket types present in country metadata. */
fun specificSocketTypesForCountry(countryInfo: CountryInfo): List<SocketType> =
    SocketType.entries.filter {
        it != SocketType.DOMESTIC && it.osmKey in countryInfo.chargingStationSocketTypes
    }

fun hasSupportedSocketTypes(countryInfo: CountryInfo): Boolean =
    specificSocketTypesForCountry(countryInfo).isNotEmpty()

/**
 * Socket types shown in the form for [countryInfo].
 * Empty when the country has no implemented motorcar connector in metadata (quest not applicable).
 * [SocketType.DOMESTIC] is appended only when usable [CountryInfo.domesticSocketType] metadata exists.
 */
fun socketTypesForCountry(countryInfo: CountryInfo): List<SocketType> {
    val specificTypes = specificSocketTypesForCountry(countryInfo)
    if (specificTypes.isEmpty()) return emptyList()
    return if (domesticPlugTypesForCountry(countryInfo).isNotEmpty()) {
        specificTypes + SocketType.DOMESTIC
    } else {
        specificTypes
    }
}
