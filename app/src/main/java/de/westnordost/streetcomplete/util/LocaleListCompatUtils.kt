package de.westnordost.streetcomplete.util

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.russhwolf.settings.ObservableSettings
import de.westnordost.streetcomplete.Prefs
import de.westnordost.streetcomplete.util.ktx.addedToFront
import java.util.Locale

/** Get the override-locale selected in this app or null if there is no override */
fun getSelectedLocale(prefs: ObservableSettings): Locale? {
    val languageTag = prefs.getString(Prefs.LANGUAGE_SELECT, "")
    return if (languageTag.isEmpty()) null else Locale.forLanguageTag(languageTag)
}

/** Get the locale selected in this app (if any) appended by the system locales */
fun getSelectedLocales(prefs: ObservableSettings): LocaleListCompat {
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
fun getSystemLocales() = ConfigurationCompat.getLocales(Resources.getSystem().configuration)
