package de.westnordost.streetcomplete.ktx

//import android.os.Build
import java.util.Locale

/**
 * Modified from: https://github.com/apache/cordova-plugin-globalization/blob/master/src/android/Globalization.java
 *
 * Returns a well-formed ITEF BCP 47 language tag representing this locale string identifier for the
 * client's current locale
 *
 * @return String: The BCP 47 language tag for the current locale
 */
fun Locale.toBcp47LanguageTag(): String? {
    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
    //    return toLanguageTag()
    //}

    var language = language
    var region = country
    var variant = variant

    // special case for Norwegian Nynorsk since "NY" cannot be a variant as per BCP 47
    // this goes before the string matching since "NY" wont pass the variant checks
    if (language == "no" && region == "NO" && variant == "NY") {
        language = "nn"
        region = "NO"
        variant = ""
    }

    if (language.isEmpty() || !language.matches(Regex("\\p{Alpha}{2,8}"))) {
        language = "und" // Follow the Locale#toLanguageTag() implementation
        // which says to return "und" for Undetermined
    } else if (language == "iw") {
        language = "he" // correct deprecated "Hebrew"
    } else if (language == "in") {
        language = "id" // correct deprecated "Indonesian"
    } else if (language == "ji") {
        language = "yi" // correct deprecated "Yiddish"
    }
    // ensure valid country code, if not well formed, it's omitted
    if (!region.matches(Regex("\\p{Alpha}{2}|\\p{Digit}{3}"))) {
        region = ""
    }
    // variant subtags that begin with a letter must be at least 5 characters long
    if (!variant.matches(Regex("\\p{Alnum}{5,8}|\\p{Digit}\\p{Alnum}{3}"))) {
        variant = ""
    }

    val bcp47Tag = mutableListOf(language)
    if (region.isNotEmpty()) bcp47Tag.add(region)
    if (variant.isNotEmpty()) bcp47Tag.add(variant)

    // we will use a dash as per BCP 47
    return bcp47Tag.joinToString("-")
}
