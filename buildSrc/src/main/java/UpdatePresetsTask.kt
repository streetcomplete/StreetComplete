import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.net.URL
import com.beust.klaxon.Parser
import com.beust.klaxon.JsonObject
import com.beust.klaxon.JsonArray
import java.io.StringWriter

/** Update the presets metadata and its translations for use with the de.westnordost:osmfeatures library */
open class UpdatePresetsTask : DefaultTask() {
    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetDir: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return
        val exportLangs = languageCodes

        // copy the presets.json 1:1
        val presetsFile = File("$targetDir/presets.json")
        presetsFile.writeText(fetchPresets())

        // download each language
        for (localizationMetadata in fetchLocalizationMetadata()) {
            val language = localizationMetadata.languageCode

            if (exportLangs != null && !exportLangs.contains(language)) continue

            println(localizationMetadata.languageCode)

            val presetsLocalization = fetchPresetsLocalizations(localizationMetadata)
            if (presetsLocalization != null) {
                val javaLanguage = bcp47LanguageTagToJavaLanguageTag(language)
                File("$targetDir/${javaLanguage}.json").writeText(presetsLocalization)
            }
        }
    }

    /** Fetch iD presets */
    private fun fetchPresets(): String {
        val presetsUrl = "https://raw.githubusercontent.com/openstreetmap/iD/develop/data/presets/presets.json"
        return URL(presetsUrl).readText()
    }

    /** Fetch relevant meta-infos for localizations from iD */
    private fun fetchLocalizationMetadata(): List<LocalizationMetadata> {
        // this file contains a list with meta information for each localization of iD
        val contentsUrl = "https://api.github.com/repos/openstreetmap/iD/contents/dist/locales"
        val languagesJson = Parser.default().parse(URL(contentsUrl).openStream()) as JsonArray<JsonObject>

        return languagesJson.mapNotNull {
            if (it["type"] == "file") {
                val name = it["name"] as String
                val languageCode = name.subSequence(0, name.lastIndexOf(".")).toString()

                LocalizationMetadata(languageCode, it["download_url"] as String)
            } else null
        }
    }

    /** Download and pick the localization for only the presets from iD localizations
     *  (the iD localizations contain everything, such as localizations of iD UI etc)*/
    private fun fetchPresetsLocalizations(localization: LocalizationMetadata): String? {
        val localizationUrl = URL(localization.downloadUrl)
        val localizationJson = Parser.default().parse(localizationUrl.openStream()) as JsonObject
        val presetsJson = localizationJson.obj(localization.languageCode)?.obj("presets")?.obj("presets")

        return if (presetsJson != null) {
            val jsonObject = JsonObject(mapOf("presets" to presetsJson))
            jsonObject.toJsonString(true).unescapeUnicode()
        } else null
    }
}

private data class LocalizationMetadata(val languageCode: String, val downloadUrl: String)

private fun String.unescapeUnicode(): String {
    val out = StringWriter(length)
    val unicode = StringBuilder(4)
    var hadSlash = false
    var inUnicode = false

    for (ch in this) {
        if (inUnicode) {
            unicode.append(ch)
            if (unicode.length == 4) {
                val unicodeChar = unicode.toString().toInt(16).toChar()
                out.write(unicodeChar.toString())
                unicode.setLength(0)
                inUnicode = false
                hadSlash = false
            }
        } else if (hadSlash) {
            hadSlash = false
            if (ch == 'u') inUnicode = true
            else {
                out.write(92)
                out.write(ch.toString())
            }
        } else if (ch == '\\') {
            hadSlash = true
        } else {
            out.write(ch.toString())
        }
    }

    if (hadSlash) out.write(92)
    return out.toString()
}