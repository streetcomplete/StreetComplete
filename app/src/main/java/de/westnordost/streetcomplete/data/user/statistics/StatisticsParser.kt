package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import org.json.JSONObject

class StatisticsParser(private val typeAliases: List<Pair<String, String>>) {
    fun parse(json: String): Statistics {
        val obj = JSONObject(json)

        val typesStatistics = parseEditTypeStatistics(obj.getJSONObject("questTypes"))
        val countriesStatistics = parseCountriesStatistics(
            obj.getJSONObject("countries"),
            obj.getJSONObject("countryRanks")
        )
        val rank = obj.getInt("rank")
        val daysActive = obj.getInt("daysActive")
        val isAnalyzing = obj.getBoolean("isAnalyzing")
        val lastUpdate = Instant.parse(obj.getString("lastUpdate"))

        val currentWeekRank = obj.getInt("currentWeekRank")
        val currentWeekTypesStatistics = parseEditTypeStatistics(obj.getJSONObject("currentWeekQuestTypes"))
        val currentWeekCountriesStatistics = parseCountriesStatistics(
            obj.getJSONObject("currentWeekCountries"),
            obj.getJSONObject("currentWeekCountryRanks")
        )

        val activeDatesRange = obj.getInt("activeDatesRange")
        val activeDatesJson = obj.getJSONArray("activeDates")
        val activeDates = ArrayList<LocalDate>(activeDatesJson.length())
        for (i in 0 until activeDatesJson.length()) {
            activeDates.add(LocalDate.parse(activeDatesJson.getString(i)))
        }

        return Statistics(
            typesStatistics,
            countriesStatistics,
            rank,
            daysActive,
            currentWeekRank,
            currentWeekTypesStatistics,
            currentWeekCountriesStatistics,
            activeDatesRange,
            activeDates,
            lastUpdate.toEpochMilliseconds(),
            isAnalyzing,
        )
    }

    private fun parseEditTypeStatistics(obj: JSONObject): List<EditTypeStatistics> {
        val typesByName: MutableMap<String, Int> = mutableMapOf()
        for (questTypeName in obj.keys()) {
            typesByName[questTypeName] = obj.getInt(questTypeName)
        }
        mergeTypeAliases(typesByName)
        return typesByName.map { EditTypeStatistics(it.key, it.value) }
    }

    private fun parseCountriesStatistics(editsObj: JSONObject, ranksObj: JSONObject): List<CountryStatistics> {
        val countries: MutableMap<String, Int> = mutableMapOf()
        for (country in editsObj.keys()) {
            countries[country] = editsObj.getInt(country)
        }
        val ranks: MutableMap<String, Int> = mutableMapOf()
        for (country in ranksObj.keys()) {
            ranks[country] = ranksObj.getInt(country)
        }
        return countries.map { CountryStatistics(it.key, it.value, ranks[it.key]) }
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
