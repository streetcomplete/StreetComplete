package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences

/** Keeps the platform's default locale in sync with the app locale setting, so that resources
 *  and formatting outside the composition use the selected locale. */
class AppLocaleUpdater(private val prefs: Preferences) {
    private var listener: SettingsListener? = null

    fun start() {
        update()
        listener = prefs.onLocaleChanged { update() }
    }

    /** (Re-)applies the current setting, e.g. after the system locale changed. */
    fun update() {
        applyAppLocale(prefs.locale)
    }
}

/** Makes [locale] the default for resources and formatting, or restores the system default. */
internal expect fun applyAppLocale(locale: Locale?)
