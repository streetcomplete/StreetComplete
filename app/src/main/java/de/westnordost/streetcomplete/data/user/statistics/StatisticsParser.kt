package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json.Default.decodeFromString

@Serializable
private data class StatisticsDTO(
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

class StatisticsParser(private val typeAliases: List<Pair<String, String>>) {
    fun parse(json: String): Statistics {
        return with(decodeFromString<StatisticsDTO>(json)) {
            Statistics(
                types = parseEditTypeStatistics(questTypes),
                countries = countries.map { (key, value) ->
                    CountryStatistics(countryCode = key, count = value, rank = countryRanks[key])
                }.sortedBy(CountryStatistics::countryCode),
                rank = rank,
                daysActive = daysActive,
                currentWeekRank = currentWeekRank,
                currentWeekTypes = parseEditTypeStatistics(currentWeekQuestTypes),
                currentWeekCountries = currentWeekCountries.map { (key, value) ->
                    CountryStatistics(countryCode = key, count = value, rank = currentWeekCountryRanks[key])
                }.sortedBy(CountryStatistics::countryCode),
                activeDatesRange = activeDatesRange,
                activeDates = activeDates,
                lastUpdate = lastUpdate.toEpochMilliseconds(),
                isAnalyzing = isAnalyzing,
            )
        }
    }

    private fun parseEditTypeStatistics(input: Map<String, Int>): List<EditTypeStatistics> {
        val result = input.toMutableMap()
        mergeTypeAliases(result)
        return result.map { EditTypeStatistics(it.key, it.value) }
    }

    private fun mergeTypeAliases(map: MutableMap<String, Int>) {
        for ((oldName, newName) in typeAliases) {
            val count = map[oldName]
            if (count != null) {
                map.remove(oldName)
                map[newName] = (map[newName] ?: 0) + count
            }
        }
    }
}
