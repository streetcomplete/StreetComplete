
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.Locale

/** Update the Android string resources (translations) for all the given language codes */
open class UpdateAppTranslationsTask : DefaultTask() {

    @get:Input var projectId: String? = null
    @get:Input var apiToken: String? = null
    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetFiles: ((androidResCode: String) -> String)? = null

    @TaskAction fun run() {
        val targetFiles = targetFiles ?: return
        val apiToken = apiToken ?: return
        val projectId = projectId ?: return
        val exportLanguages = languageCodes?.map { Locale.forLanguageTag(it) }

        val languageTags = fetchAvailableLocalizations(apiToken, projectId).map { it.code }
        for (languageTag in languageTags) {
            val locale = Locale.forLanguageTag(languageTag)

            if (exportLanguages != null && !exportLanguages.any { it == locale }) continue
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
"    <string name=\"$key\">\"${value.escapeXml().replace("\"", "\\\"")}\"</string>"
} }
</resources>"""
            for (androidResCode in androidResCodes) {
                File(targetFiles(androidResCode)).writeText(text)
            }
        }
    }
}
