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
    @SerialName("ton") TON("TONS"),
    @SerialName("short ton") SHORT_TON("TONS"),
    @SerialName("pound") POUND("POUNDS"),
}

@Serializable
data class IncompleteCountryInfo(
    // this value is not defined in the yaml file but it is the ISO language code part of the file name!
    // i.e. US for US-TX.yml
    val countryCode: String,

    // sorted alphabetically for better overview
    val additionalStreetsignLanguages: List<String> = emptyList(),
    val additionalValidHousenumberRegex: String? = null,
    val advisorySpeedLimitSignStyle: String? = null,
    val atmOperators: List<String> = emptyList(),
    val centerLineStyle: String? = null,
    val chargingStationOperators: List<String> = emptyList(),
    val clothesContainerOperators: List<String> = emptyList(),
    val edgeLineStyle: String? = null,
    val firstDayOfWorkweek: String? = null,
    val hasAdvisorySpeedLimitSign: Boolean? = null,
    val hasBiWeeklyAlternateSideParkingSign: Boolean? = null,
    val hasCenterLeftTurnLane: Boolean? = null,
    val hasDailyAlternateSideParkingSign: Boolean? = null,
    val hasLivingStreet: Boolean? = null,
    val hasNoStandingSign: Boolean? = null,
    val hasSlowZone: Boolean? = null,
    val isLeftHandTraffic: Boolean? = null,
    val isUsuallyAnyGlassRecyclableInContainers: Boolean? = null,
    val lengthUnits: List<LengthUnit> = emptyList(),
    val livingStreetSignStyle: String? = null,
    val mobileCountryCode: Int? = null,
    val noParkingLineStyle: String? = null,
    val noParkingSignStyle: String? = null,
    val noStandingLineStyle: String? = null,
    val noStandingSignStyle: String? = null,
    val noStoppingLineStyle: String? = null,
    val noStoppingSignStyle: String? = null,
    val officialLanguages: List<String> = emptyList(),
    val orchardProduces: List<String> = emptyList(),
    val popularReligions: List<String> = emptyList(),
    val popularSports: List<String> = emptyList(),
    val regularShoppingDays: Int? = null,
    val roofsAreUsuallyFlat: Boolean? = null,
    val slowZoneLabelPosition: String? = null,
    val slowZoneLabelText: String? = null,
    val speedUnits: List<SpeedMeasurementUnit> = emptyList(),
    val weightLimitUnits: List<WeightMeasurementUnit> = emptyList(),
    val workweekDays: Int? = null,
)

data class CountryInfo(private val infos: List<IncompleteCountryInfo>) {
    val countryCode get() = infos.getFirstNonEmpty { it.countryCode }

    // part of default.yml, so cannot be null or empty
    val advisorySpeedLimitSignStyle get() = infos.getFirstNonEmpty { it.advisorySpeedLimitSignStyle }!!
    val centerLineStyle get() = infos.getFirstNonEmpty { it.centerLineStyle }!!
    val edgeLineStyle get() = infos.getFirstNonEmpty { it.edgeLineStyle }!!
    val firstDayOfWorkweek get() = infos.getFirstNonEmpty { it.firstDayOfWorkweek }!!
    val hasAdvisorySpeedLimitSign get() = infos.getFirstNonEmpty { it.hasAdvisorySpeedLimitSign }!!
    val hasBiWeeklyAlternateSideParkingSign get() = infos.getFirstNonEmpty { it.hasBiWeeklyAlternateSideParkingSign }!!
    val hasCenterLeftTurnLane get() = infos.getFirstNonEmpty { it.hasCenterLeftTurnLane }!!
    val hasDailyAlternateSideParkingSign get() = infos.getFirstNonEmpty { it.hasDailyAlternateSideParkingSign }!!
    val hasLivingStreet get() = infos.getFirstNonEmpty { it.hasLivingStreet }!!
    val hasNoStandingSign get() = infos.getFirstNonEmpty { it.hasNoStandingSign }!!
    val hasSlowZone get() = infos.getFirstNonEmpty { it.hasSlowZone }!!
    val isLeftHandTraffic get() = infos.getFirstNonEmpty { it.isLeftHandTraffic }!!
    val isUsuallyAnyGlassRecyclableInContainers get() = infos.getFirstNonEmpty { it.isUsuallyAnyGlassRecyclableInContainers }!!
    val lengthUnits get() = infos.getFirstNonEmpty { it.lengthUnits }
    val noParkingSignStyle get() = infos.getFirstNonEmpty { it.noParkingSignStyle }!!
    val noStoppingSignStyle get() = infos.getFirstNonEmpty { it.noStoppingSignStyle }!!
    val officialLanguages get() = infos.getFirstNonEmpty { it.officialLanguages }
    val popularReligions get() = infos.getFirstNonEmpty { it.popularReligions }
    val regularShoppingDays get() = infos.getFirstNonEmpty { it.regularShoppingDays }!!
    val roofsAreUsuallyFlat get() = infos.getFirstNonEmpty { it.roofsAreUsuallyFlat }!!
    val speedUnits get() = infos.getFirstNonEmpty { it.speedUnits }
    val weightLimitUnits get() = infos.getFirstNonEmpty { it.weightLimitUnits }
    val workweekDays get() = infos.getFirstNonEmpty { it.workweekDays }!!

    // may be null or empty
    val additionalStreetsignLanguages get() = infos.getFirstNonEmpty { it.additionalStreetsignLanguages }
    val additionalValidHousenumberRegex get() = infos.getFirstNonEmpty { it.additionalValidHousenumberRegex }
    val atmOperators get() = infos.getFirstNonEmpty { it.atmOperators }
    val chargingStationOperators get() = infos.getFirstNonEmpty { it.chargingStationOperators }
    val clothesContainerOperators get() = infos.getFirstNonEmpty { it.clothesContainerOperators }
    val livingStreetSignStyle get() = infos.getFirstNonEmpty { it.livingStreetSignStyle }
    val mobileCountryCode get() = infos.getFirstNonEmpty { it.mobileCountryCode }
    val noParkingLineStyle get() = infos.getFirstNonEmpty { it.noParkingLineStyle }
    val noStandingLineStyle get() = infos.getFirstNonEmpty { it.noStandingLineStyle }
    val noStandingSignStyle get() = infos.getFirstNonEmpty { it.noStandingSignStyle }
    val noStoppingLineStyle get() = infos.getFirstNonEmpty { it.noStoppingLineStyle }
    val orchardProduces get() = infos.getFirstNonEmpty { it.orchardProduces }
    val popularSports get() = infos.getFirstNonEmpty { it.popularSports }
    val slowZoneLabelPosition get() = infos.getFirstNonEmpty { it.slowZoneLabelPosition }
    val slowZoneLabelText get() = infos.getFirstNonEmpty { it.slowZoneLabelText }

    val locale: Locale
        get() = if (officialLanguages.isEmpty()) {
            Locale.getDefault()
        } else {
            Locale(officialLanguages[0], countryCode)
        }
}

private fun <T> List<IncompleteCountryInfo>.getFirstNonEmpty(getValue: (IncompleteCountryInfo) -> T): T {
    for (index in 0 until this.size - 1) {
        val value = getValue(this[index])
        if (value != null && (value !is List<*> || value.isNotEmpty())) return value
    }

    return getValue(this.last())
}
