package de.westnordost.streetcomplete

import android.content.res.Resources
import android.os.LocaleList
import java.util.Locale

internal actual fun applyAppLanguage(language: String?) {
    val locales = appLocales(language)
    // Compose Locale.current and non-composable resource/formatting code read these defaults.
    Locale.setDefault(locales[0])
    LocaleList.setDefault(locales)
}

/** The system locales with [language] put in front, if any. */
internal fun appLocales(language: String?): LocaleList {
    val systemLocales = Resources.getSystem().configuration.locales
    if (language == null) return systemLocales
    val locales = listOf(Locale.forLanguageTag(language)) +
        (0 until systemLocales.size()).map { systemLocales[it]!! }
    return LocaleList(*locales.distinct().toTypedArray())
}
