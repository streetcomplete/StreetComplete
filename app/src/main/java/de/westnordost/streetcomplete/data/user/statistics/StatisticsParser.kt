package de.westnordost.streetcomplete.data.user.statistics

import kotlinx.datetime.Instant
import org.json.JSONObject

class StatisticsParser(private val typeAliases: List<Pair<String, String>>) {
    fun parse(json: String): Statistics {
        val obj = JSONObject(json)
        val questTypesJson = obj.getJSONObject("questTypes")
        val typesByName: MutableMap<String, Int> = mutableMapOf()
        for (questTypeName in questTypesJson.keys()) {
            typesByName[questTypeName] = questTypesJson.getInt(questTypeName)
        }
        mergeTypeAliases(typesByName)
        val typesStatistics = typesByName.map { EditTypeStatistics(it.key, it.value) }
        val countriesJson = obj.getJSONObject("countries")
        val countries: MutableMap<String, Int> = mutableMapOf()
        for (country in countriesJson.keys()) {
            countries[country] = countriesJson.getInt(country)
        }
        val countryRanksJson = obj.getJSONObject("countryRanks")
        val countryRanks: MutableMap<String, Int> = mutableMapOf()
        for (country in countryRanksJson.keys()) {
            countryRanks[country] = countryRanksJson.getInt(country)
        }
        val countriesStatistics = countries.map { CountryStatistics(it.key, it.value, countryRanks[it.key]) }
        val rank = obj.getInt("rank")
        val daysActive = obj.getInt("daysActive")
        val isAnalyzing = obj.getBoolean("isAnalyzing")
        val lastUpdate = Instant.parse(obj.getString("lastUpdate"))
        return Statistics(typesStatistics, countriesStatistics, rank, daysActive, lastUpdate.toEpochMilliseconds(), isAnalyzing)
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
