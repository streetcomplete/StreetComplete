import java.io.StringWriter
import java.util.*



// Java (and thus also Android) uses some old iso (language) codes. F.e. id -> in etc.
// so the localized files also need to use the old iso codes
fun bcp47LanguageTagToJavaLanguageTag(bcp47: String): String {
    val locale = Locale.forLanguageTag(bcp47)
    var result = locale.language
    if (locale.script.isNotEmpty()) result += "-" + locale.script
    if (locale.country.isNotEmpty()) result += "-" + locale.country
    return result
}

fun javaLanguageTagToAndroidResCodes(languageTag: String): List<String> {
    val locale = Locale.forLanguageTag(languageTag)
    // scripts not supported by Android resource system
    if (locale.script.isNotEmpty()) return listOf()

    if (languageTag == "nb")    return listOf("no", "nb")
    if (languageTag == "zh-CN") return listOf("zh")
    val withCountry = Regex("([a-z]{2,3})-([A-Z]{2})").matchEntire(languageTag)
    if (withCountry != null) {
        return listOf(withCountry.groupValues[1] + "-r" + withCountry.groupValues[2])
    }
    return listOf(languageTag)
}
