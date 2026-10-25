import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Export the strings used in the Info.plist of an iOS or macOS app to
 *  <lang>.lproj/InfoPlist.strings files in the target directory */
open class UpdateInfoPlistTranslationsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var strings: Map<String, String>
    @get:Input lateinit var languageCodes: Collection<String>
    @get:OutputDirectory lateinit var targetDir: File

    @TaskAction fun run() {
        require(apiToken != "invalid") { "POEditor API token must be set" }

        val exportLanguages = languageCodes.map { Locale.forLanguageTag(it) }

        val languageTags = fetchAvailableLocalizations(apiToken, projectId).map { it.code }

        // language code -> (string id -> translation string)
        val translations = HashMap<String, Map<String, String>>()

        for (languageTag in languageTags) {
            val lang = if (languageTag.lowercase() == "en-us") "en" else languageTag
            val locale = Locale.forLanguageTag(transformPOEditorLanguageTag(lang))

            if (!exportLanguages.any { it == locale }) continue

            val actualLanguageTag = locale.toLanguageTag()

            print(languageTag)
            if (actualLanguageTag != languageTag) print(" -> " + actualLanguageTag)
            println()

            translations[actualLanguageTag] = fetchLocalizationJson(apiToken, projectId, languageTag)
        }

        // remove all prior translations
        targetDir.listFiles { file -> file.name.endsWith(".lproj") }?.forEach { dir ->
            dir.resolve("InfoPlist.strings").delete()
            dir.delete() // only if empty
        }

        for ((languageCode, translation) in translations) {
            // plistKey: e.g. "NSLocationWhenInUseUsageDescription"
            // stringId: e.g. "no_location_permission_warning"
            val lines = strings.mapNotNull { (plistKey, stringId) ->
                val string = translation[stringId] ?: return@mapNotNull null
                "\"$plistKey\" = \"${string.escapeStrings()}\";"
            }
            if (lines.isEmpty()) continue

            val file = targetDir.resolve("$languageCode.lproj/InfoPlist.strings")
            file.parentFile.mkdirs()
            file.writeText(lines.joinToString("\n", postfix = "\n"))
        }
    }
}

private fun String.escapeStrings(): String =
    replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
