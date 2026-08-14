package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.LocaleList

fun getLanguagesForFeatureDictionary(): List<String?> {
    val localeList = LocaleList.current.localeList
    val result = ArrayList<String?>(localeList.size + 2)
    result.addAll(localeList.map { it.toLanguageTag() })
    /* add fallback to English if (some) English is not part of the locale list already as the
       fallback for text is also always English in this app (strings.xml) independent of, or rather
       additionally to what is in the user's LocaleList. */
    if (result.none { it == "en" }) {
        result.add("en")
    }
    // add null to allow unlocalized features
    result.add(null)
    return result
}
