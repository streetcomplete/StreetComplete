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

/**
 * Countries where the current [SocketType.DOMESTIC] illustration (Type E/F / CEE 7) is the
 * usual household plug. StreetComplete has no household-plug country metadata, so this is an
 * explicit allow-list rather than inventing a worldwide plug database.
 *
 * Excludes e.g. GB/IE (Type G), CH/LI (Type J), IT/SM/VA (Type L), DK (Type K),
 * AU/NZ (Type I), IL (Type H), and non-European Type F users such as KR.
 */
private val typeEFDomesticCountryCodes = setOf(
    "AD", "AL", "AT", "BA", "BE", "BG", "CZ", "DE", "EE", "ES", "FI", "FR", "GR", "HR", "HU",
    "IS", "LT", "LU", "LV", "MC", "ME", "MK", "NL", "NO", "PL", "PT", "RO", "RS", "SE", "SI",
    "SK", "XK",
)

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
 * [SocketType.DOMESTIC] is appended only in Type E/F countries (see [typeEFDomesticCountryCodes]).
 */
fun socketTypesForCountry(countryInfo: CountryInfo): List<SocketType> {
    val specificTypes = specificSocketTypesForCountry(countryInfo)
    if (specificTypes.isEmpty()) return emptyList()
    val countryCode = countryInfo.countryCode
    return if (countryCode != null && countryCode in typeEFDomesticCountryCodes) {
        specificTypes + SocketType.DOMESTIC
    } else {
        specificTypes
    }
}
