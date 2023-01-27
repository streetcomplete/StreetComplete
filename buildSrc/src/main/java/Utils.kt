import kotlinx.serialization.Serializable
import java.net.URL
import java.util.Locale

fun Locale.toAndroidResCodes(): List<String> {
    // Android (sometimes?) uses the old ISO 639 codes for the resource system
    val languages = when (language) {
        "nb", "no" -> listOf("no", "nb") // Norwegian / Norwegian Bokmal
        "id", "in" -> listOf("id", "in") // Indonesian
        "he", "iw" -> listOf("he", "iw") // Hebrew
        "yi", "ji" -> listOf("yi", "ji") // Yiddish
        else -> listOf(language)
    }

    if (script.isNotEmpty()) {
        val countryStr = if (country.isNotEmpty()) "+$country" else ""
        return languages.map { "b+$it+$script$countryStr" }
    } else {
        val countryStr = if (country.isNotEmpty()) "-r$country" else ""
        return languages.map { it + countryStr }
    }
}

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
