
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update the Android string resources (translations) for all the given language codes */
open class UpdateAppTranslationsTask : DefaultTask() {

    @get:Input lateinit var projectId: String
    @get:Input lateinit var apiToken: String
    @get:Input lateinit var languageCodes: Collection<String>
    @get:Input var targetFileFns: List<((androidResCode: String) -> String)>? = null

    @TaskAction fun run() {
        val targetFileFns = targetFileFns ?: return
        val exportLanguages = languageCodes.map { Locale.forLanguageTag(it) }

        val languageTags = fetchAvailableLocalizations(apiToken, projectId).map { it.code }
        for (languageTag in languageTags) {
            val locale = Locale.forLanguageTag(languageTag)

            if (!exportLanguages.any { it == locale }) continue
            // en-us is the source language
            if (locale == Locale.US) continue

            val androidResCodes = locale.transformPOEditorLanguageTag().toAndroidResCodes()

            print(languageTag)
            if (androidResCodes.singleOrNull() != languageTag) print(" -> " + androidResCodes.joinToString(", "))
            println()

            // download the translation and save it in the appropriate directory
            val translations = fetchLocalizationJson(apiToken, projectId, languageTag)
            val text = """<?xml version="1.0" encoding="utf-8"?>
<resources>
${translations.entries.joinToString("\n") { (key, value) ->
"    <string name=\"$key\">${value.escapeXml().replace("'", "\\'")}</string>"
} }
</resources>"""
            for (androidResCode in androidResCodes) {
                for (targetFileFn in targetFileFns) {
                    val file = File(targetFileFn(androidResCode))
                    File(file.parent).mkdirs()
                    file.writeText(text)
                }
            }
        }
    }
}
