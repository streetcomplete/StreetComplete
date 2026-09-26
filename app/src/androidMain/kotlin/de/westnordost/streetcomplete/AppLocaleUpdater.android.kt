package de.westnordost.streetcomplete

import android.content.res.Resources
import android.os.LocaleList
import androidx.compose.ui.text.intl.Locale

internal actual fun applyAppLocale(locale: Locale?) {
    val locales = appLocales(locale)
    // Compose Locale.current and non-composable resource/formatting code read these defaults.
    java.util.Locale.setDefault(locales[0])
    LocaleList.setDefault(locales)
}

/** The system locales with [locale] put in front, if any. */
internal fun appLocales(locale: Locale?): LocaleList {
    val systemLocales = Resources.getSystem().configuration.locales
    if (locale == null) return systemLocales
    val locales = listOf(locale.platformLocale) +
        (0 until systemLocales.size()).map { systemLocales[it]!! }
    return LocaleList(*locales.distinct().toTypedArray())
}
