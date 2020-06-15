package de.westnordost.streetcomplete.data.user

import java.util.Date

data class Statistics(
    val questTypes: Map<String, Int>,
    val countries: List<CountryStatistics>,
    val rank: Int,
    val daysActive: Int,
    val lastUpdate: Date,
    val isAnalyzing: Boolean
)

data class CountryStatistics(val countryCode: String, val solvedCount: Int, val rank: Int?)