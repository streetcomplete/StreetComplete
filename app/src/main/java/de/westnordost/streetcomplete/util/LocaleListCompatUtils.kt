package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.util.ktx.addedToFront
import java.util.Locale

/** Get the override-locale selected in this app or null if there is no override */
fun getSelectedLocale(prefs: Preferences): Locale? {
    val languageTag = prefs.language ?: ""
    return if (languageTag.isEmpty()) null else Locale.forLanguageTag(languageTag)
}

/** Get the locale selected in this app (if any) appended by the system locales */
fun getSelectedLocales(prefs: Preferences): LocaleListCompat {
    val locale = getSelectedLocale(prefs)
    val systemLocales = getSystemLocales()
    return if (locale == null) systemLocales else systemLocales.addedToFront(locale)
}

/** Set locales of this configuration */
fun Configuration.setLocales(locales: LocaleListCompat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        setLocales(locales.unwrap() as LocaleList)
    } else {
        setLocale(if (locales.isEmpty) null else locales[0])
    }
}

/** Set default Java locale(s). locales must not be empty */
fun setDefaultLocales(locales: LocaleListCompat) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        LocaleList.setDefault(locales.unwrap() as LocaleList)
    } else {
        Locale.setDefault(locales[0])
    }
}

/** Get Android system locale(s) */
fun getSystemLocales(): LocaleListCompat =
    ConfigurationCompat.getLocales(Resources.getSystem().configuration)
