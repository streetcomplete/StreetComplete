package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.data.preferences.Preferences

/** Applies the selected locale to resources and formatting for the lifetime of the application. */
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

/** Applies [locale] to resources and formatting, or restores the system defaults. */
internal expect fun applyAppLocale(locale: Locale?)
