package de.westnordost.streetcomplete.data.meta

import androidx.compose.ui.text.intl.Locale
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class LengthUnit(private val abbr: String) {
    @SerialName("meter") METER("m"),
    @SerialName("foot and inch") FOOT_AND_INCH("ft / in");

    override fun toString() = abbr
}

@Serializable
enum class SpeedMeasurementUnit(val displayString: String) {
    @SerialName("kilometers per hour") KILOMETERS_PER_HOUR("km/h"),
    @SerialName("miles per hour") MILES_PER_HOUR("mph");

    override fun toString() = displayString
}

@Serializable
enum class WeightMeasurementUnit(val displayString: String) {
    @SerialName("ton") METRIC_TON("TONS"),
    @SerialName("short ton") SHORT_TON("TONS"),
    @SerialName("pound") POUND("POUNDS"),
}

@Serializable
data class IncompleteCountryInfo(
    // this value is not defined in the yaml file but it is the ISO language code part of the file name!
    // e.g. US for US-TX.yml
    val countryCode: String,

    // sorted alphabetically for better overview
    val additionalStreetsignLanguages: List<String>? = null,
    val additionalValidHousenumberRegex: String? = null,
    val advisoryCycleLaneStyle: String? = null,
    val advisorySpeedLimitSignStyle: String? = null,
    val atmOperators: List<String>? = null,
    val centerLineStyle: String? = null,
    val chargingStationOperators: List<String>? = null,
    val clothesContainerOperators: List<String>? = null,
    val edgeLineStyle: String? = null,
    val exclusiveCycleLaneStyle: String? = null,
    val hasAdvisorySpeedLimitSign: Boolean? = null,
    val hasBiWeeklyAlternateSideParkingSign: Boolean? = null,
    val hasCenterLeftTurnLane: Boolean? = null,
    val hasAdvisoryCycleLane: Boolean? = null,
    val hasBicycleBoulevard: Boolean? = null,
    val hasDailyAlternateSideParkingSign: Boolean? = null,
    val hasLivingStreet: Boolean? = null,
    val hasNoStandingSign: Boolean? = null,
    val hasSlowZone: Boolean? = null,
    val isLeftHandTraffic: Boolean? = null,
    val isUsuallyAnyGlassRecyclableInContainers: Boolean? = null,
    val lengthUnits: List<LengthUnit>? = null,
    val livingStreetSignStyle: String? = null,
    val mobileCountryCode: Int? = null,
    val noEntrySignStyle: String? = null,
    val noParkingLineStyle: String? = null,
    val noParkingSignStyle: String? = null,
    val noStandingLineStyle: String? = null,
    val noStandingSignStyle: String? = null,
    val noStoppingLineStyle: String? = null,
    val noStoppingSignStyle: String? = null,
    val officialLanguages: List<String>? = null,
    val orchardProduces: List<String>? = null,
    val parcelLockerBrand: List<String>? = null,
    val pictogramCycleLaneStyle: String? = null,
    val popularReligions: List<String>? = null,
    val popularSports: List<String>? = null,
    val postboxesHaveCollectionTimes: Boolean? = null,
    val postboxesHaveRef: Boolean? = null,
    val postboxesHaveRoyalCypher: Boolean? = null,
    val regularShoppingDays: Int? = null,
    val roofsAreUsuallyFlat: Boolean? = null,
    val slowZoneLabelPosition: String? = null,
    val slowZoneLabelText: String? = null,
    val speedUnits: List<SpeedMeasurementUnit>? = null,
    val weightLimitUnits: List<WeightMeasurementUnit>? = null,
    val workweek: String? = null,
)

data class CountryInfo(private val infos: List<IncompleteCountryInfo>) {
    val countryCode get() = infos.first().countryCode

    // part of default.yml, so cannot be null
    val advisorySpeedLimitSignStyle: String =
         infos.firstNotNullOf { it.advisorySpeedLimitSignStyle }
    val centerLineStyle: String =
         infos.firstNotNullOf { it.centerLineStyle }
    val edgeLineStyle: String =
         infos.firstNotNullOf { it.edgeLineStyle }
    val exclusiveCycleLaneStyle: String =
         infos.firstNotNullOf { it.exclusiveCycleLaneStyle }
    val workweek: String =
         infos.firstNotNullOf { it.workweek }
    val hasAdvisoryCycleLane: Boolean =
         infos.firstNotNullOf { it.hasAdvisoryCycleLane }
    val hasAdvisorySpeedLimitSign: Boolean =
         infos.firstNotNullOf { it.hasAdvisorySpeedLimitSign }
    val hasBicycleBoulevard: Boolean =
         infos.firstNotNullOf { it.hasBicycleBoulevard }
    val hasBiWeeklyAlternateSideParkingSign: Boolean =
         infos.firstNotNullOf { it.hasBiWeeklyAlternateSideParkingSign }
    val hasCenterLeftTurnLane: Boolean =
         infos.firstNotNullOf { it.hasCenterLeftTurnLane }
    val hasDailyAlternateSideParkingSign: Boolean =
         infos.firstNotNullOf { it.hasDailyAlternateSideParkingSign }
    val hasLivingStreet: Boolean =
         infos.firstNotNullOf { it.hasLivingStreet }
    val hasNoStandingSign: Boolean =
         infos.firstNotNullOf { it.hasNoStandingSign }
    val hasSlowZone: Boolean =
         infos.firstNotNullOf { it.hasSlowZone }
    val isLeftHandTraffic: Boolean =
         infos.firstNotNullOf { it.isLeftHandTraffic }
    val isUsuallyAnyGlassRecyclableInContainers: Boolean =
         infos.firstNotNullOf { it.isUsuallyAnyGlassRecyclableInContainers }
    val lengthUnits: List<LengthUnit> =
         infos.firstNotNullOf { it.lengthUnits }
    val noEntrySignStyle: String =
         infos.firstNotNullOf { it.noEntrySignStyle }
    val noParkingSignStyle: String =
         infos.firstNotNullOf { it.noParkingSignStyle }
    val noStoppingSignStyle: String =
         infos.firstNotNullOf { it.noStoppingSignStyle }
    val officialLanguages: List<String> =
         infos.firstNotNullOf { it.officialLanguages }
    val pictogramCycleLaneStyle: String =
         infos.firstNotNullOf { it.pictogramCycleLaneStyle }
    val popularReligions: List<String> =
         infos.firstNotNullOf { it.popularReligions }
    val postboxesHaveCollectionTimes: Boolean =
         infos.firstNotNullOf { it.postboxesHaveCollectionTimes }
    val postboxesHaveRef: Boolean =
         infos.firstNotNullOf { it.postboxesHaveRef }
    val postboxesHaveRoyalCypher: Boolean =
         infos.firstNotNullOf { it.postboxesHaveRoyalCypher }
    val regularShoppingDays: Int =
         infos.firstNotNullOf { it.regularShoppingDays }
    val roofsAreUsuallyFlat: Boolean =
         infos.firstNotNullOf { it.roofsAreUsuallyFlat }
    val speedUnits: List<SpeedMeasurementUnit> =
         infos.firstNotNullOf { it.speedUnits }
    val weightLimitUnits: List<WeightMeasurementUnit> =
         infos.firstNotNullOf { it.weightLimitUnits }

    // may be null
    val additionalStreetsignLanguages: List<String> =
         infos.firstNotNullOfOrNull { it.additionalStreetsignLanguages } ?: emptyList()
    val additionalValidHousenumberRegex: String? =
         infos.firstNotNullOfOrNull { it.additionalValidHousenumberRegex }
    val advisoryCycleLaneStyle: String? =
         infos.firstNotNullOfOrNull { it.advisoryCycleLaneStyle }
    val atmOperators: List<String>? =
         infos.firstNotNullOfOrNull { it.atmOperators }
    val chargingStationOperators: List<String>? =
         infos.firstNotNullOfOrNull { it.chargingStationOperators }
    val clothesContainerOperators: List<String>? =
         infos.firstNotNullOfOrNull { it.clothesContainerOperators }
    val livingStreetSignStyle: String? =
         infos.firstNotNullOfOrNull { it.livingStreetSignStyle }
    val mobileCountryCode: Int? =
         infos.firstNotNullOfOrNull { it.mobileCountryCode }
    val noParkingLineStyle: String? =
         infos.firstNotNullOfOrNull { it.noParkingLineStyle }
    val noStandingLineStyle: String? =
         infos.firstNotNullOfOrNull { it.noStandingLineStyle }
    val noStandingSignStyle: String? =
         infos.firstNotNullOfOrNull { it.noStandingSignStyle }
    val noStoppingLineStyle: String? =
         infos.firstNotNullOfOrNull { it.noStoppingLineStyle }
    val orchardProduces: List<String> =
         infos.firstNotNullOfOrNull { it.orchardProduces } ?: emptyList()
    val parcelLockerBrand: List<String>? =
         infos.firstNotNullOfOrNull { it.parcelLockerBrand }
    val popularSports: List<String> =
         infos.firstNotNullOfOrNull { it.popularSports } ?: emptyList()
    val slowZoneLabelPosition: String? =
         infos.firstNotNullOfOrNull { it.slowZoneLabelPosition }
    val slowZoneLabelText: String? =
         infos.firstNotNullOfOrNull { it.slowZoneLabelText }

    val language: String? =
         officialLanguages.firstOrNull()

    val languageTag: String?
        get() {
            val lang = language ?: return null
            return "$lang-$countryCode"
        }

    /** the country locale, but preferring the user's set language if the country has several
     *  official languages and the user selected one of them, e.g. French in Switzerland */
    val userPreferredLocale: Locale
        get() {
            if (officialLanguages.isEmpty()) return Locale.current

            val locales = officialLanguages.map { Locale("$it-$countryCode") }
            val preferredLocale = locales.find { it.language == Locale.current.language }
            return preferredLocale ?: locales.first()
        }
}
