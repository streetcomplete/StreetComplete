
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.util.*

/** Update the Android string resources (translations) for all the given language codes */
open class UpdateAppTranslationsTask : AUpdateFromPOEditorTask() {

    @get:Input var languageCodes: Collection<String>? = null
    @get:Input var targetFiles: ((androidResCode: String) -> String)? = null

    @TaskAction fun run() {
        val targetFiles = targetFiles ?: return
        val exportLangs = languageCodes
            ?.map { it.toLowerCase(Locale.US) }
            // don't export en, it is the source language
            ?.filter { it != "en" }

        val languageCodes = fetchLocalizations { it["code"] as String }
        for (languageCode in languageCodes) {

            if (exportLangs == null || exportLangs.contains(languageCode.toLowerCase(Locale.US))) {
                println(languageCode)

                val javaLanguageTag = bcp47LanguageTagToJavaLanguageTag(languageCode)
                val androidResCodes = javaLanguageTagToAndroidResCodes(javaLanguageTag)

                // download the translation and save it in the appropriate directory
                val text = fetchLocalization(languageCode, "android_strings") { inputStream ->
                    inputStream.readBytes().toString(Charsets.UTF_8)
                }
                for (androidResCode in androidResCodes) {
                    File(targetFiles(androidResCode)).writeText(text)
                }
            }
        }
    }
}
