import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.StringWriter
import java.net.URL
import java.util.Locale

/** Update the presets metadata and its translations for use with the de.westnordost:osmfeatures
 *  library */
open class UpdatePresetsTask : DefaultTask() {
    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetDir: String? = null
    @get:Input var version: String? = null

    @TaskAction fun run() {
        val targetDir = targetDir ?: return
        /* eagerly also fetch different variants of a language (e.g. "en-NZ" also when just "en"
           is specified as well as "sr" if just "sr-Cyrl" is specified). Hence, we only look at the
           language code */
        val exportLanguages = languageCodes?.map { Locale(Locale.forLanguageTag(it).language) }
        val version = version ?: return

        // copy and reduce the presets.json
        val presetsFile = File("$targetDir/presets.json")
        presetsFile.writeText(fetchAndReducePresets(version))

        // download each language
        val localizationMetadataList = fetchLocalizationMetadata()
        for (localizationMetadata in localizationMetadataList) {
            val locale = localizationMetadata.locale
            val languageLocale = Locale(locale.language)
            if (exportLanguages != null && !exportLanguages.any { it == languageLocale }) continue

            val javaLanguageTag = locale.toLanguageTag()
            println(javaLanguageTag)

            val presetsLocalization = fetchAndReducePresetsLocalizations(localizationMetadata)
            File("$targetDir/$javaLanguageTag.json").writeText(presetsLocalization)
        }

        // Norway has two languages, one of them is called Bokmål
        // coded "no" in iD presets, but "nb" is also expected by Android.
        // https://github.com/streetcomplete/StreetComplete/issues/3890
        if ("no" in languageCodes.orEmpty()) {
            val bokmalFile = File("$targetDir/no.json")
            bokmalFile.copyTo(File("$targetDir/nb.json"), overwrite = true)
        }
    }

    /** Fetch iD presets */
    private fun fetchAndReducePresets(version: String): String {
        val presetsUrl = "https://raw.githubusercontent.com/openstreetmap/id-tagging-schema/$version/dist/presets.json"
        val json = Parser.default().parse(URL(presetsUrl).openStream()) as JsonObject
        // remove unused presets
        json.entries.removeAll { (key, value) ->
            // we don't need them templates
            key.startsWith("@templates")
            // remove presets specific to certain countries (these are very likely just tweaks
            // which fields are displayed etc), see https://github.com/ideditor/schema-builder/issues/94#issuecomment-2416796047
            || (value as JsonObject).obj("locationSet")?.array<String>("include") != null
        }
        // strip unused fields
        for (value in json.values) {
            val preset = value as JsonObject
            preset.remove("fields")
            preset.remove("moreFields")
            preset.remove("reference")
            // after the locationSet->include presets are removed (see above), what remains are
            // locationSet->exclude presets (preset available in all countries except X). These
            // are usually the counterpart of the tweaks made for different countries, so with the
            // tweaks removed, the locationSet->exclude field can be removed, too
            preset.remove("locationSet")
        }
        return json.toJsonString(prettyPrint = true)
    }

    /** Fetch relevant meta-infos for localizations from iD */
    private fun fetchLocalizationMetadata(): List<LocalizationMetadata> {
        // this file contains a list with meta information for each localization of iD
        val contentsUrl = "https://api.github.com/repos/openstreetmap/id-tagging-schema/contents/dist/translations"
        val languagesJson = Parser.default().parse(URL(contentsUrl).openStream()) as JsonArray<JsonObject>

        return languagesJson.mapNotNull {
            if (it["type"] != "file") return@mapNotNull null
            val name = it["name"] as String
            if (name.endsWith(".min.json")) return@mapNotNull null

            val filename = name.subSequence(0, name.indexOf(".")).toString()
            val locale = Locale.forLanguageTag(filename.replace('@', '-'))
            LocalizationMetadata(locale, it["download_url"] as String)
        }
    }

    /** Download and pick the localization for only the preset features because the other things
     *  are not used (currently) */
    private fun fetchAndReducePresetsLocalizations(localization: LocalizationMetadata): String {
        val json = Parser.default().parse(URL(localization.downloadUrl).openStream()) as JsonObject
        for (value in json.values) {
            val language = value as JsonObject
            val presets = language.obj("presets")
            // translations of preset categories and preset fields not used, so let's delete them
            presets?.remove("categories")
            presets?.remove("fields")
        }
        return json.toJsonString(prettyPrint = true).unescapeUnicode()
    }
}

private data class LocalizationMetadata(val locale: Locale, val downloadUrl: String)

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
            if (ch == 'u') {
                inUnicode = true
            } else {
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
