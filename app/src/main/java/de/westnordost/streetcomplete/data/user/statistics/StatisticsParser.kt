package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import org.json.JSONObject
import java.time.OffsetDateTime

class StatisticsParser(
    private val questTypeRegistry: QuestTypeRegistry,
    private val questAliases: List<Pair<String, String>>
) {
    fun parse(json: String): Statistics {
        val obj = JSONObject(json)
        val questTypesJson = obj.getJSONObject("questTypes")
        val questTypesByName: MutableMap<String, Int> = mutableMapOf()
        for (questTypeName in questTypesJson.keys()) {
            questTypesByName[questTypeName] = questTypesJson.getInt(questTypeName)
        }
        mergeQuestAliases(questTypesByName)
        val questTypes = questTypesByName.mapNotNull {
            val questType = questTypeRegistry.getByName(it.key)
            if (questType != null) QuestTypeStatistics(questType, it.value) else null
        }

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
        val lastUpdate = OffsetDateTime.parse(obj.getString("lastUpdate")).toInstant()
        return Statistics(questTypes, countriesStatistics, rank, daysActive, lastUpdate.toEpochMilli(), isAnalyzing)
    }

    private fun mergeQuestAliases(map: MutableMap<String, Int>) {
        for ((oldName, newName) in questAliases) {
            val count = map[oldName]
            if (count != null) {
                map.remove(oldName)
                map[newName] = (map[newName] ?: 0) + count
            }
        }
    }
}
