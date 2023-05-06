package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Statistics(
    val types: List<EditTypeStatistics>,
    val countries: List<CountryStatistics>,
    val rank: Int,
    val daysActive: Int,
    val currentWeekRank: Int,
    val currentWeekTypes: List<EditTypeStatistics>,
    val currentWeekCountries: List<CountryStatistics>,
    val activeDatesRange: Int,
    val activeDates: List<LocalDate>,
    val lastUpdate: Long,
    val isAnalyzing: Boolean,
)

@Serializable
data class CountryStatistics(val countryCode: String, val count: Int, val rank: Int?)

@Serializable
data class EditTypeStatistics(val type: String, val count: Int)
