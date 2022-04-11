import java.util.Locale

fun Locale.toAndroidResCodes(): List<String> {
    // scripts are not supported by Android resource system
    if (script.isNotEmpty()) return listOf()
    // Android (sometimes?) uses the old ISO 639 codes for the resource system
    val languages = when (language) {
        "nb", "no" -> listOf("no", "nb") // Norwegian / Norwegian Bokmal
        "id", "in" -> listOf("id", "in") // Indonesian
        "he", "iw" -> listOf("he", "iw") // Hebrew
        "yi", "ji" -> listOf("yi", "ji") // Yiddish
        else -> listOf(language)
    }
    val countryStr = if (country.isNotEmpty()) "-r$country" else ""
    return languages.map { it + countryStr }
}
