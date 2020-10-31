package de.westnordost.streetcomplete.data.user

data class Statistics(
    val questTypes: Map<String, Int>,
    val countries: List<CountryStatistics>,
    val rank: Int,
    val daysActive: Int,
    val lastUpdate: Long,
    val isAnalyzing: Boolean
)

data class CountryStatistics(val countryCode: String, val solvedCount: Int, val rank: Int?)