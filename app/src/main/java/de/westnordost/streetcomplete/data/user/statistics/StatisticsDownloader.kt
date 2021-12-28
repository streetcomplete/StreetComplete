package de.westnordost.streetcomplete.data.user.statistics

import de.westnordost.streetcomplete.ApplicationConstants
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** Downloads statistics from the backend */
class StatisticsDownloader(
    private val baseUrl: String,
    private val statisticsParser: StatisticsParser
) {
    fun download(osmUserId: Long): Statistics {
        (URL("$baseUrl?user_id=$osmUserId").openConnection() as HttpURLConnection).run {
            useCaches = false
            doOutput = true
            doInput = true
            setRequestProperty("User-Agent", ApplicationConstants.USER_AGENT)
            requestMethod = "GET"
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    return statisticsParser.parse(inputStream.bufferedReader().use { it.readText() })
                }
                else -> {
                    val errorMessage = responseMessage
                    val errorDescription = errorStream?.bufferedReader()?.use { it.readText() }
                    throw IOException("$responseCode $errorMessage: $errorDescription")
                }
            }
        }
    }
}
