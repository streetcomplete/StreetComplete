import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

/** Export the strings used in the Info.plist of the macOS app to <lang>.lproj/InfoPlist.strings
 *  files in the target directory */
open class UpdateMacosAppTranslationsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var strings: Map<String, String>
    @get:Input lateinit var languageCodes: Collection<String>
    @get:OutputDirectory lateinit var targetDir: File

    @TaskAction fun run() {
        require(apiToken != "invalid") { "POEditor API token must be set" }

        // language code -> (string id -> translation string)
        val translations = fetchLocalizationsJson(apiToken, projectId, languageCodes)

        // remove all prior translations
        targetDir.listFiles { file -> file.name.endsWith(".lproj") }?.forEach { it.deleteRecursively() }

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
