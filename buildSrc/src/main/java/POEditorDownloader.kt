import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/** Fetch the localizations for the given language code and return it as a map of strings */
fun fetchLocalizationJson(apiToken: String, projectId: String, languageCode: String): Map<String, String> {
    val code = if (languageCode == "sr-latn") "sr-cyrl" else languageCode

    val url = URL(fetchLocalizationDownloadUrl(apiToken, projectId, code, "key_value_json"))
    return url.retryingQuotaConnection(null) { inputSteam ->
        val txt = inputSteam.bufferedReader().use { it.readText() }
        if (txt.isEmpty()) return@retryingQuotaConnection mapOf()
        val obj = Parser.default().parse(txt.reader()) as JsonObject
        val result = obj.entries.associate { it.key to it.value as String }

        if (languageCode == "sr-latn") {
            result.mapValues { it.value.serbianCyrillicToLatin() }
        } else {
            result
        }
    }
}

/** Fetch language codes of available translations from POEditor API */
fun fetchAvailableLocalizations(apiToken: String, projectId: String): List<Localization> {
    val result = URL("https://api.poeditor.com/v2/languages/list").retryingQuotaConnection({ connection ->
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.outputStream.bufferedWriter().use { it.write("api_token=$apiToken&id=$projectId") }
    }) { inputStream ->
        val response = Parser.default().parse(inputStream) as JsonObject
        response.obj("result")!!.array<JsonObject>("languages")!!.map {
            Localization(it.string("code")!!, it.int("translations")!!, it.int("percentage")!!)
        }
    }.toMutableList()

    val serbian = result.find { it.code == "sr-cyrl" }
    if (serbian != null) {
        result.add(serbian.copy(code = "sr-latn"))
    }
    return result
}

data class Localization(val code: String, val translations: Int, val percentage: Int)

/** Fetch the download URL for the given language code. Handle quota. */
private fun fetchLocalizationDownloadUrl(apiToken: String, projectId: String, languageCode: String, format: String): String {
    return URL("https://api.poeditor.com/v2/projects/export").retryingQuotaConnection({ connection ->
        connection.doOutput = true
        connection.requestMethod = "POST"
        connection.outputStream.bufferedWriter().use { it.write(
            "api_token=$apiToken&id=$projectId&language=${languageCode.toLowerCase(Locale.US)}&type=$format&filters=translated"
        ) }
    }) { inputStream ->
        val response = Parser.default().parse(inputStream) as JsonObject
        (response.obj("result")!!)["url"] as String
    }
}

// this is for waiting and retrying for quota to replenish when querying POEditor API... :-|
private fun <T> URL.retryingQuotaConnection(setup: ((HttpURLConnection) -> Unit)? = null, block: (InputStream) -> T): T {
    val maxWait = 12
    var i = 0
    while (i++ < maxWait) {
        val connection = openConnection() as HttpURLConnection
        setup?.invoke(connection)
        if (connection.responseCode == 429) {
            connection.disconnect()
            Thread.sleep(5000)
        } else {
            val result = block(connection.inputStream)
            connection.disconnect()
            return result
        }
    }
    throw Exception("POEditor API continues to report http status code 429")
}

fun Locale.transformPOEditorLanguageTag() = when (toLanguageTag()) {
    "sr-Cyrl" -> Locale("sr") // Serbian is referred to as Serbian (Cyrillic) in POEditor
    "zh-CN" -> Locale("zh") // Chinese is referred to as Chinese (China) in POEditor
    else -> this
}
