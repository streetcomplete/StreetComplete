package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.LocalDate
import kotlinx.io.Source
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlin.time.Instant

class StatisticsParser {
    private val jsonParser = Json { ignoreUnknownKeys = true }

    @OptIn(ExperimentalSerializationApi::class)
    fun parse(json: Source): Statistics =
        jsonParser.decodeFromSource<ApiStatistics>(json).toStatistics()

    private fun ApiStatistics.toStatistics() = Statistics(
        types = questTypes.map { EditTypeStatistics(it.key, it.value) },
        countries = countries.map { (key, value) ->
            CountryStatistics(countryCode = key, count = value, rank = countryRanks[key])
        }.sortedBy(CountryStatistics::countryCode),
        rank = rank,
        daysActive = daysActive,
        currentWeekRank = currentWeekRank,
        currentWeekTypes = currentWeekQuestTypes.map { EditTypeStatistics(it.key, it.value) },
        currentWeekCountries = currentWeekCountries.map { (key, value) ->
            CountryStatistics(countryCode = key, count = value, rank = currentWeekCountryRanks[key])
        }.sortedBy(CountryStatistics::countryCode),
        activeDatesRange = activeDatesRange,
        activeDates = activeDates,
        lastUpdate = lastUpdate.toEpochMilliseconds(),
        isAnalyzing = isAnalyzing,
    )
}

@Serializable
private data class ApiStatistics(
    val questTypes: Map<String, Int>,
    val countries: Map<String, Int>,
    val countryRanks: Map<String, Int>,
    val rank: Int,
    val currentWeekRank: Int,
    val currentWeekQuestTypes: Map<String, Int>,
    val currentWeekCountries: Map<String, Int>,
    val currentWeekCountryRanks: Map<String, Int>,
    val daysActive: Int,
    val activeDatesRange: Int,
    val activeDates: List<LocalDate>,
    val lastUpdate: Instant,
    val isAnalyzing: Boolean,
)
