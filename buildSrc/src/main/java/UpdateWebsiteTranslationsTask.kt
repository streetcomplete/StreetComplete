import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File

/** Update the metadata that contain the store descriptions for the app (for F-Droid) */
open class UpdateWebsiteTranslationsTask : AUpdateFromPOEditorTask() {

    @get:Input var targetDir: String? = null

    private val requiredKeys = setOf(
        "store_listing_short_description",
        "store_listing_full_description",

        "about_title_privacy_statement",
        "privacy_html",
        "privacy_html_tileserver2",
        "privacy_html_statistics",
        "privacy_html_third_party_quest_sources",
        "privacy_html_image_upload2",
    )

    private val keys = setOf(
        "about_title_donate",
        "about_summary_donate",

        "about_title_translate",
        "about_title_repository",
        "about_title_faq",
        "title_whats_new",
        "action_about2",
        "about_title_get_involved",
        "urlconfig_scan_qr_code_again2"
    )

    @TaskAction fun run() {
        val targetDir = targetDir ?: return

        val languageCodes = fetchLocalizations { it["code"] as String }
        val json = Json {
            prettyPrint = true
        }

        for (languageCode in languageCodes) {
            println(languageCode)
            fetchLocalization(languageCode, "key_value_json") { inputStream ->
                val txt = inputStream.bufferedReader().use { it.readText() }
                if (txt.isNotEmpty()) {
                    val translations = json.decodeFromString<Map<String, String>>(txt)
                    val lang = if (languageCode.toLowerCase() == "en-us") "en" else languageCode.toLowerCase()
                    val strings = translations.filterKeys { it in keys || it in requiredKeys }
                    // only accept complete translations
                    if (requiredKeys.all { it in strings.keys }) {
                        val dir = File("$targetDir/$lang/")
                        dir.mkdirs()
                        val file = File(dir, "strings.json")
                        file.writeText(json.encodeToString(strings))
                    }
                }
            }
        }
    }
}
