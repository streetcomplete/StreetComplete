import kotlinx.serialization.Serializable
import java.net.URL
import java.util.Locale

fun Locale.toAndroidResCode(): String {
    if (script.isNotEmpty()) {
        val countryStr = if (country.isNotEmpty()) "+$country" else ""
        return "b+$language+$script$countryStr"
    } else {
        val countryStr = if (country.isNotEmpty()) "-r$country" else ""
        return language + countryStr
    }
}

/** Android resource codes of this locale and of all the locales in [fallbackLanguages] that fall
 *  back to it */
fun Locale.toAndroidResCodes(fallbackLanguages: Map<String, String>): List<String> =
    listOf(toAndroidResCode()) + fallbackLanguages
        .filterValues { Locale.forLanguageTag(it) == this }
        .keys.map { Locale.forLanguageTag(it).toAndroidResCode() }

fun URL.readText() = openConnection().getInputStream().bufferedReader().readText()

fun String.escapeXml(): String =
    asSequence().joinToString("") { when (it) {
        '<' -> "&lt;"
        '>' -> "&gt;"
        '&' -> "&amp;"
        else -> it.toString()
    } }

@Serializable
data class GithubDirectoryListingItem(
    /** File or directory name */
    val name: String,
    /** Path including file or directory name, relative to repository root */
    val path: String,
    /** Whether it is a file or a directory */
    val type: String,
    /** URL to the "contents" API endpoint for this item */
    val url: String,
    /** URL for the raw file contents, null for directories */
    val download_url: String?
)
