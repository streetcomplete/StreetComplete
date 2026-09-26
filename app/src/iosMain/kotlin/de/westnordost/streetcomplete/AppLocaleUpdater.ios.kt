package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import de.westnordost.streetcomplete.util.locale.appFormattingLocale
import platform.Foundation.NSArgumentDomain
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.preferredLanguages

internal actual fun applyAppLocale(locale: Locale?) {
    IosAppLocale.apply(locale)
}

private object IosAppLocale {
    private const val LANGUAGES_KEY = "AppleLanguages"
    private val defaults = NSUserDefaults.standardUserDefaults

    init {
        // Older versions persisted this derived setting. Preferences already stores the selection.
        defaults.removeObjectForKey(LANGUAGES_KEY)
    }

    private val originalLanguages = defaults.volatileDomainForName(NSArgumentDomain)[LANGUAGES_KEY]
    private val systemLanguages = NSLocale.preferredLanguages.filterIsInstance<String>()
    private val systemFormattingLocale = NSLocale.currentLocale

    fun apply(locale: Locale?) {
        appFormattingLocale = locale?.toNSLocale() ?: systemFormattingLocale

        // Compose reads AppleLanguages. Keep the override process-local: persisting it changes
        // Foundation's startup locale, which cannot be reset when returning to system default.
        val domain = defaults.volatileDomainForName(NSArgumentDomain)
        val updated = domain.toMutableMap()
        val languages = locale?.let { (listOf(it.toLanguageTag()) + systemLanguages).distinct() }
            ?: originalLanguages
        if (languages == null) updated.remove(LANGUAGES_KEY)
        else updated[LANGUAGES_KEY] = languages

        // Defaults notifications are synchronous. An unchanged update must not write again when
        // Multiplatform Settings re-enters its listener before updating the listener's cached value.
        if (updated != domain) defaults.setVolatileDomain(updated, NSArgumentDomain)
    }
}
