package de.westnordost.streetcomplete.data.user.statistics

data class Statistics(
    val types: List<EditTypeStatistics>,
    val countries: List<CountryStatistics>,
    val rank: Int,
    val daysActive: Int,
    val currentWeekRank: Int,
    val currentWeekTypes: List<EditTypeStatistics>,
    val currentWeekCountries: List<CountryStatistics>,
    val lastUpdate: Long,
    val isAnalyzing: Boolean,
)

data class CountryStatistics(val countryCode: String, val count: Int, val rank: Int?)

data class EditTypeStatistics(val type: String, val count: Int)
