package de.westnordost.streetcomplete.data.meta

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.Locale

@Serializable
enum class LengthUnit(private val abbr: String) {
    @SerialName("meter") METER("m"),
    @SerialName("foot and inch") FOOT_AND_INCH("ft / in");

    override fun toString() = abbr
}

@Serializable
enum class SpeedMeasurementUnit(private val displayString: String) {
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
    // i.e. US for US-TX.yml
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
    val firstDayOfWorkweek: String? = null,
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
    val workweekDays: Int? = null,
)

data class CountryInfo(private val infos: List<IncompleteCountryInfo>) {
    val countryCode get() = infos.first().countryCode

    // part of default.yml, so cannot be null
    val advisorySpeedLimitSignStyle: String
        get() = infos.firstNotNullOf { it.advisorySpeedLimitSignStyle }
    val centerLineStyle: String
        get() = infos.firstNotNullOf { it.centerLineStyle }
    val edgeLineStyle: String
        get() = infos.firstNotNullOf { it.edgeLineStyle }
    val exclusiveCycleLaneStyle: String
        get() = infos.firstNotNullOf { it.exclusiveCycleLaneStyle }
    val firstDayOfWorkweek: String
        get() = infos.firstNotNullOf { it.firstDayOfWorkweek }
    val hasAdvisoryCycleLane: Boolean
        get() = infos.firstNotNullOf { it.hasAdvisoryCycleLane }
    val hasAdvisorySpeedLimitSign: Boolean
        get() = infos.firstNotNullOf { it.hasAdvisorySpeedLimitSign }
    val hasBicycleBoulevard: Boolean
        get() = infos.firstNotNullOf { it.hasBicycleBoulevard }
    val hasBiWeeklyAlternateSideParkingSign: Boolean
        get() = infos.firstNotNullOf { it.hasBiWeeklyAlternateSideParkingSign }
    val hasCenterLeftTurnLane: Boolean
        get() = infos.firstNotNullOf { it.hasCenterLeftTurnLane }
    val hasDailyAlternateSideParkingSign: Boolean
        get() = infos.firstNotNullOf { it.hasDailyAlternateSideParkingSign }
    val hasLivingStreet: Boolean
        get() = infos.firstNotNullOf { it.hasLivingStreet }
    val hasNoStandingSign: Boolean
        get() = infos.firstNotNullOf { it.hasNoStandingSign }
    val hasSlowZone: Boolean
        get() = infos.firstNotNullOf { it.hasSlowZone }
    val isLeftHandTraffic: Boolean
        get() = infos.firstNotNullOf { it.isLeftHandTraffic }
    val isUsuallyAnyGlassRecyclableInContainers: Boolean
        get() = infos.firstNotNullOf { it.isUsuallyAnyGlassRecyclableInContainers }
    val lengthUnits: List<LengthUnit>
        get() = infos.firstNotNullOf { it.lengthUnits }
    val noEntrySignStyle: String
        get() = infos.firstNotNullOf { it.noEntrySignStyle }
    val noParkingSignStyle: String
        get() = infos.firstNotNullOf { it.noParkingSignStyle }
    val noStoppingSignStyle: String
        get() = infos.firstNotNullOf { it.noStoppingSignStyle }
    val officialLanguages: List<String>
        get() = infos.firstNotNullOf { it.officialLanguages }
    val pictogramCycleLaneStyle: String
        get() = infos.firstNotNullOf { it.pictogramCycleLaneStyle }
    val popularReligions: List<String>
        get() = infos.firstNotNullOf { it.popularReligions }
    val postboxesHaveCollectionTimes: Boolean
        get() = infos.firstNotNullOf { it.postboxesHaveCollectionTimes }
    val postboxesHaveRef: Boolean
        get() = infos.firstNotNullOf { it.postboxesHaveRef }
    val postboxesHaveRoyalCypher: Boolean
        get() = infos.firstNotNullOf { it.postboxesHaveRoyalCypher }
    val regularShoppingDays: Int
        get() = infos.firstNotNullOf { it.regularShoppingDays }
    val roofsAreUsuallyFlat: Boolean
        get() = infos.firstNotNullOf { it.roofsAreUsuallyFlat }
    val speedUnits: List<SpeedMeasurementUnit>
        get() = infos.firstNotNullOf { it.speedUnits }
    val weightLimitUnits: List<WeightMeasurementUnit>
        get() = infos.firstNotNullOf { it.weightLimitUnits }
    val workweekDays: Int
        get() = infos.firstNotNullOf { it.workweekDays }

    // may be null
    val additionalStreetsignLanguages: List<String>
        get() = infos.firstNotNullOfOrNull { it.additionalStreetsignLanguages } ?: emptyList()
    val additionalValidHousenumberRegex: String?
        get() = infos.firstNotNullOfOrNull { it.additionalValidHousenumberRegex }
    val advisoryCycleLaneStyle: String?
        get() = infos.firstNotNullOfOrNull { it.advisoryCycleLaneStyle }
    val atmOperators: List<String>?
        get() = infos.firstNotNullOfOrNull { it.atmOperators }
    val chargingStationOperators: List<String>?
        get() = infos.firstNotNullOfOrNull { it.chargingStationOperators }
    val clothesContainerOperators: List<String>?
        get() = infos.firstNotNullOfOrNull { it.clothesContainerOperators }
    val livingStreetSignStyle: String?
        get() = infos.firstNotNullOfOrNull { it.livingStreetSignStyle }
    val mobileCountryCode: Int?
        get() = infos.firstNotNullOfOrNull { it.mobileCountryCode }
    val noParkingLineStyle: String?
        get() = infos.firstNotNullOfOrNull { it.noParkingLineStyle }
    val noStandingLineStyle: String?
        get() = infos.firstNotNullOfOrNull { it.noStandingLineStyle }
    val noStandingSignStyle: String?
        get() = infos.firstNotNullOfOrNull { it.noStandingSignStyle }
    val noStoppingLineStyle: String?
        get() = infos.firstNotNullOfOrNull { it.noStoppingLineStyle }
    val orchardProduces: List<String>
        get() = infos.firstNotNullOfOrNull { it.orchardProduces } ?: emptyList()
    val parcelLockerBrand: List<String>?
        get() = infos.firstNotNullOfOrNull { it.parcelLockerBrand }
    val popularSports: List<String>
        get() = infos.firstNotNullOfOrNull { it.popularSports } ?: emptyList()
    val slowZoneLabelPosition: String?
        get() = infos.firstNotNullOfOrNull { it.slowZoneLabelPosition }
    val slowZoneLabelText: String?
        get() = infos.firstNotNullOfOrNull { it.slowZoneLabelText }

    val locale: Locale
        get() = if (officialLanguages.isEmpty()) {
            Locale.getDefault()
        } else {
            Locale(officialLanguages[0], countryCode)
        }

    /** the country locale, but preferring the user's set language if the country has several
     *  official languages and the user selected one of them, e.g. French in Switzerland */
    val userPreferredLocale: Locale get() {
        if (officialLanguages.isEmpty()) return Locale.getDefault()

        val locales = officialLanguages.map { Locale(it, countryCode) }
        val preferredLocale = locales.find { it.language == Locale.getDefault().language }
        return preferredLocale ?: locales.first()
    }
}
