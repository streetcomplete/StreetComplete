package de.westnordost.streetcomplete

import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences

/** Keeps the platform's default locale in sync with the app language setting, so that resources
 *  and formatting outside the composition use the selected language. */
class AppLocaleUpdater(private val prefs: Preferences) {
    private var listener: SettingsListener? = null

    fun start() {
        update()
        listener = prefs.onLanguageChanged { update() }
    }

    /** (Re-)applies the current setting, e.g. after the system locale changed. */
    fun update() {
        applyAppLanguage(prefs.language)
    }
}

/** Makes [language] the default for resources and formatting, or restores the system default. */
internal expect fun applyAppLanguage(language: String?)
