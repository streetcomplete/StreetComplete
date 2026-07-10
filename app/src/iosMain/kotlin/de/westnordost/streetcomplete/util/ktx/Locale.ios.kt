package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.NSLocaleScriptCode

/** Unlike in Jetpack Compose on Android, Locale.platformLocale is internal in Compose
 *  Multiplatform, so the NSLocale must be re-created from the language tag */
val Locale.nsLocale: NSLocale get() = NSLocale(toLanguageTag())

actual fun Locale.getDisplayName(locale: Locale): String? =
    locale.nsLocale.displayNameForKey(NSLocaleIdentifier, locale.toLanguageTag())

actual fun Locale.getDisplayLanguage(locale: Locale): String? =
    locale.nsLocale.displayNameForKey(NSLocaleLanguageCode, locale.language)

actual fun Locale.getDisplayRegion(locale: Locale): String? =
    locale.nsLocale.displayNameForKey(NSLocaleCountryCode, locale.region)

actual fun Locale.getDisplayScript(locale: Locale): String? =
    locale.nsLocale.displayNameForKey(NSLocaleScriptCode, locale.script)
