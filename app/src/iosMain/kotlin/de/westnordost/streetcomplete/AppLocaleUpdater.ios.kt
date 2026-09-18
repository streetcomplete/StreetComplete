package de.westnordost.streetcomplete

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.appFormattingLocale
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages

internal actual fun applyAppLanguage(language: String?) {
    val defaults = NSUserDefaults.standardUserDefaults
    // Read the system list without our previous override.
    defaults.removeObjectForKey("AppleLanguages")
    val systemLanguages = NSLocale.preferredLanguages.filterIsInstance<String>()
    if (language != null) defaults.setObject((listOf(language) + systemLanguages).distinct(), "AppleLanguages")
    appFormattingLocale = language?.let(::Locale)
}
