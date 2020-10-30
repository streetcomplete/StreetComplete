package de.westnordost.streetcomplete.data.user

import de.westnordost.streetcomplete.ApplicationConstants
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

/** Downloads statistics from the backend */
class StatisticsDownloader(private val baseUrl: String) {

    private val lastActivityDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US)

    fun download(osmUserId: Long): Statistics {
        (URL("$baseUrl?user_id=$osmUserId").openConnection() as HttpURLConnection).run {
            useCaches = false
            doOutput = true
            doInput = true
            setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT)
            requestMethod = "GET"
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    return parse(inputStream.bufferedReader().use { it.readText() })
                }
                else -> {
                    val errorMessage = responseMessage
                    val errorDescription = errorStream?.bufferedReader()?.use { it.readText() }
                    throw IOException("$responseCode $errorMessage: $errorDescription")
                }
            }
        }
    }

    private fun parse(json: String): Statistics {
        val obj = JSONObject(json)
        val questTypesJson = obj.getJSONObject("questTypes")
        val questTypes: MutableMap<String, Int> = mutableMapOf()
        for (questType in questTypesJson.keys()) {
            questTypes[questType] = questTypesJson.getInt(questType)
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
        val lastUpdate = lastActivityDateFormat.parse(obj.getString("lastUpdate"))
        return Statistics(questTypes, countriesStatistics, rank, daysActive, lastUpdate, isAnalyzing)
    }
}
