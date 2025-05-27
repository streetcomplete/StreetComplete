package de.westnordost.streetcomplete.data.meta

import java.util.Locale

/** the country locale, but preferring the user's set language if the country has several
 *  official languages and the user selected one of them, e.g. French in Switzerland */
val CountryInfo.userPreferredLocale: Locale get() {
    if (officialLanguages.isEmpty()) return Locale.getDefault()

    val locales = officialLanguages.map { Locale(it, countryCode) }
    val preferredLocale = locales.find { it.language == Locale.getDefault().language }
    return preferredLocale ?: locales.first()
}
