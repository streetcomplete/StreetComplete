package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.resources.Res
import de.westnordost.streetcomplete.ui.ktx.readYaml
import kotlinx.coroutines.runBlocking

/** Applies the selected locale to resources and formatting for the lifetime of the application. */
class AppLocaleUpdater(private val prefs: Preferences) {
    private var listener: SettingsListener? = null

    fun start() {
        update()
        listener = prefs.onLocaleChanged { update() }
    }

    /** (Re-)applies the current setting, e.g. after the system locale changed. */
    fun update() {
        val locale = prefs.locale
        if (locale != null && !locale.isSupportedAppLocale(supportedLanguageTags)) prefs.locale = null
        applyAppLocale(prefs.locale)
    }
}

/** Applies [locale] to resources and formatting, or restores the system defaults. */
internal expect fun applyAppLocale(locale: Locale?)

private val supportedLanguageTags by lazy {
    runBlocking { Res.readYaml<List<String>>("files/languages.yml") }
}

// Keep the resource tags as strings: NSLocale can drop scripts when canonicalizing them.
private fun Locale.isSupportedAppLocale(languageTags: List<String>): Boolean {
    val tag = toLanguageTag()
    return languageTags.any { tag.equals(it, true) || tag.startsWith("$it-", true) }
}

internal fun resolveAppLocale(
    locale: Locale?,
    systemLocales: List<Locale>,
    languageTags: List<String> = supportedLanguageTags,
): Locale = (listOfNotNull(locale) + systemLocales)
    .firstOrNull { it.isSupportedAppLocale(languageTags) } ?: Locale("en")
