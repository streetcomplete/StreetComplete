import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.esotericsoftware.yamlbeans.YamlWriter
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.FileWriter
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.Locale

open class GetTranslatorCreditsTask : DefaultTask() {
    @get:Input lateinit var targetFile: String
    @get:Input lateinit var languageCodes: Collection<String>

    @TaskAction
    fun run() {
        // map of language tag -> translator name -> translation count
        val resultMap: MutableMap<String, MutableMap<String, Int>> = mutableMapOf()

        // POEditor displays language names. What we need however are language tags
        val tagsByName = languageCodes.associateBy { tagToName(it) }

        // 1. get all users and their ids
        val users = queryUsersOnAllPages()

        // 2. for each user, query his contributions and add it to the result map
        for (user in users) {
            val translationsByName = queryTranslatorStats(user.id)!!
            translationsByName.forEach { (languageName, count) ->
                val tag = tagsByName.getValue(languageName)
                resultMap.getOrPut(tag, { mutableMapOf() })[user.name] = count
            }
        }

        // 3. write the result map to file
        val fileWriter = FileWriter(targetFile, false)
        fileWriter.write("# Do not edit. This file is generated\n")
        val writer = YamlWriter(fileWriter)
        writer.write(resultMap)
        writer.close()
        fileWriter.close()
    }
}

data class User(
    val id: Int,
    val name: String
)

/** Scrape all contributors HTML pages for the user names and ids */
private fun queryUsersOnAllPages(): List<User> {
    val result = mutableListOf<User>()
    var i = 1
    while (true) {
        val users = queryUsers(i++)
        if (users.isEmpty()) break
        result += users
    }
    return result
}

/** Scrape the given contributors HTML page for the user names and ids */
private fun queryUsers(pageIndex: Int): List<User> {
    val url = URL("https://poeditor.com/contributors/?page=$pageIndex")
    val connection = url.openConnection() as HttpURLConnection
    try {
        connection.setRequestProperty("User-Agent", "StreetComplete")
        connection.setRequestProperty("Accept-Language", "en-US,en")
        connection.doOutput = true

        val result = mutableListOf<User>()
        // TODO scrape. Use jsoup? Or is it simple enough for default Java DOM parser?
        // find <div>s with class=contributor-wrapper
        // -> in each, find <span> with class=user-name         <- child text node this is the name
        // -> in each, find an <a> with attribute data-user     <- value of attribute is the user id
        // (-> in each, find an <img> with class=avatar-24      <- url is the url to gravatar img)
        return result
    } finally {
        connection.disconnect()
    }
}

/** Convert language tag to how the language is named in English POEditor UI
 *  f.e. en -> English, en-US -> English (US). */
private fun tagToName(tag: String): String {
    val locale = Locale.forLanguageTag(tag)
    val countryName = if (locale.country != "") " ("+locale.country+")" else ""
    val langName = locale.getDisplayLanguage(Locale.ENGLISH)
    return langName + countryName
}

/** returns a map of POEditor language name to number of characters translated, f.e.
 *  "Portuguese (BR)" -> 123
 *  "German" -> 12 */
private fun queryTranslatorStats(userId: Int): Map<String, Int>? {
    // TODO there is also the URL "https://poeditor.com/contributors/export_stats" - what does it do?
    // TODO These are all the headers reverse-engineered. Not sure if all these are necessary. Let's start with the minimum
    // TODO however, the PHPSESSID/cookies must be set...
    //Accept: application/json, text/javascript, */*; q=0.01
    //Accept-Encoding: gzip, deflate, br
    //X-Requested-With: XMLHttpRequest
    //Content-Length: 61
    //Origin: https://poeditor.com
    //Connection: keep-alive
    //Referer: https://poeditor.com/contributors/?id_project=&id_language=&status=
    //Cookie: _POEditor_contributors_chart_type=area-chart; PHPSESSID=6le1lsa3h5p6kk09cn9ks8f8u9; login=132853%3A%242y%2410%24qocgH2LLKjdKhTtM8w.Hqui3otoVyLVTE4ElgC85lr%2FGaANT2DOuS; consent_to_cookies=1; projects=%5B%5D; contributors=%7B%22p%22%3A%22%22%2C%22ls%22%3A%22%22%2C%22s%22%3A%22%22%7D

    val url = URL("https://poeditor.com/contributors/contributor_stats")
    val connection = url.openConnection() as HttpURLConnection
    val today = LocalDate.now().toString()
    try {
        connection.setRequestProperty("User-Agent", "StreetComplete")
        connection.setRequestProperty("Accept-Language", "en-US,en")
        connection.doOutput = true
        connection.useCaches = false
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
        connection.setRequestProperty("charset", "utf-8")
        connection.outputStream.bufferedWriter().use { it.write(
            "id_project=97843&start=2016-01-01&end=$today&user=$userId"
        ) }
        val response = Parser.default().parse(connection.inputStream) as JsonObject
        val languages = response.obj("table")?.array<JsonObject>("languages")
        return languages?.mapNotNull {
            val chars = it.obj("chars")?.obj("target")?.int("unformat")
            val language = it.obj("language")?.string("name")
            if (chars != null && language != null) {
                language to chars
            } else null
        }?.associate { it }
    } finally {
        connection.disconnect()
    }
}
