package de.westnordost.streetcomplete.util

import android.content.res.Resources
import android.os.LocaleList
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.ktx.addedToFront
import java.util.Locale

/** Get the override-locale selected in this app or null if there is no override */
fun getSelectedLocale(prefs: Preferences): Locale? {
    val languageTag = prefs.language ?: ""
    return if (languageTag.isEmpty()) null else Locale.forLanguageTag(languageTag)
}

/** Get the locale selected in this app (if any) appended by the system locales */
fun getSelectedLocales(prefs: Preferences): LocaleList {
    val locale = getSelectedLocale(prefs)
    val systemLocales = getSystemLocales()
    return if (locale == null) systemLocales else systemLocales.addedToFront(locale)
}

/** Get Android system locale(s) */
fun getSystemLocales(): LocaleList =
    Resources.getSystem().configuration.locales
