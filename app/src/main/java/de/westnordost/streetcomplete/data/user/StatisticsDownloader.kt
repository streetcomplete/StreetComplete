package de.westnordost.streetcomplete.data.user

import de.westnordost.osmapi.common.Iso8601CompatibleDateFormat
import de.westnordost.streetcomplete.ApplicationConstants
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Downloads statistics from the backend */
class StatisticsDownloader(private val baseUrl: String) {

    private val lastActivityDateFormat = Iso8601CompatibleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")

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
        val daysActive = obj.getInt("daysActive")
        val isAnalyzing = obj.getBoolean("isAnalyzing")
        val lastUpdate = lastActivityDateFormat.parse(obj.getString("lastUpdate"))
        return Statistics(questTypes, daysActive, lastUpdate, isAnalyzing)
    }
}
