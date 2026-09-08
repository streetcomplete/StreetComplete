package de.westnordost.streetcomplete.quests.charging_station_socket

import de.westnordost.streetcomplete.data.meta.CountryInfo
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource

/**
 * Household plug types from [CountryInfo.domesticSocketType] (countrymetadata).
 * These are presentation-only; the quest still writes [SocketType.DOMESTIC] as `socket:domestic`.
 */
enum class DomesticPlugType(val osmValue: String) {
    TYPEC("typec"),
    SCHUKO("schuko"),
    TYPEE("typee"),
    TYPEK("typek"),
    TYPEL("typeL"),
    BS1363("bs1363"),
    SEV1011_T13("sev1011_t13"),
    NEMA_1_15("nema_1_15"),
    NEMA_5_15("nema_5_15"),
    NEMA_5_20("nema_5_20"),
    GB1002("gb1002"),
    AS3112("as3112");

    companion object {
        private val byOsmValue = entries.associateBy { it.osmValue }

        fun fromOsmValue(value: String): DomesticPlugType? = byOsmValue[value]
    }
}

val DomesticPlugType.icon: DrawableResource
    get() = when (this) {
        DomesticPlugType.TYPEC -> Res.drawable.socket_domestic_typec
        DomesticPlugType.SCHUKO -> Res.drawable.socket_domestic
        DomesticPlugType.TYPEE -> Res.drawable.socket_domestic_typee
        DomesticPlugType.TYPEK -> Res.drawable.socket_domestic_typek
        DomesticPlugType.TYPEL -> Res.drawable.socket_domestic_typel
        DomesticPlugType.BS1363 -> Res.drawable.socket_domestic_bs1363
        DomesticPlugType.SEV1011_T13 -> Res.drawable.socket_domestic_sev1011_t13
        DomesticPlugType.NEMA_1_15 -> Res.drawable.socket_domestic_nema_1_15
        DomesticPlugType.NEMA_5_15 -> Res.drawable.socket_domestic_nema_5_15
        DomesticPlugType.NEMA_5_20 -> Res.drawable.socket_domestic_nema_5_20
        DomesticPlugType.GB1002 -> Res.drawable.socket_domestic_gb1002
        DomesticPlugType.AS3112 -> Res.drawable.socket_domestic_as3112
    }

/** Known household plug types for this country, ignoring unsupported metadata values. */
fun domesticPlugTypesForCountry(countryInfo: CountryInfo): List<DomesticPlugType> =
    countryInfo.domesticSocketType.mapNotNull { DomesticPlugType.fromOsmValue(it) }

fun domesticPlugIcons(countryInfo: CountryInfo): List<DrawableResource> =
    domesticPlugTypesForCountry(countryInfo).map { it.icon }
