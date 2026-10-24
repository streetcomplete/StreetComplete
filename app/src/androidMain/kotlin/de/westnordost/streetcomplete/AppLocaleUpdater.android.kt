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

/** The system locales with the resolved app locale put in front. */
internal fun appLocales(locale: Locale?): LocaleList {
    val configurationLocales = Resources.getSystem().configuration.locales
    val systemLocales = (0 until configurationLocales.size()).map { configurationLocales[it]!! }
    val resolvedLocale = resolveAppLocale(locale, systemLocales.map { Locale(it.toLanguageTag()) })
    val locales = listOf(resolvedLocale.platformLocale) + systemLocales
    return LocaleList(*locales.distinct().toTypedArray())
}
