package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.NSLocaleScriptCode

/** Unlike in Jetpack Compose on Android, Locale.platformLocale is internal in Compose
 *  Multiplatform, so the NSLocale must be re-created from the language tag */
fun Locale.toNSLocale() = NSLocale(toLanguageTag())

actual fun Locale.getDisplayName(locale: Locale): String? =
    locale.toNSLocale().displayNameForKey(NSLocaleIdentifier, toLanguageTag())

actual fun Locale.getDisplayLanguage(locale: Locale): String? =
    locale.toNSLocale().displayNameForKey(NSLocaleLanguageCode, language)

actual fun Locale.getDisplayRegion(locale: Locale): String? =
    locale.toNSLocale().displayNameForKey(NSLocaleCountryCode, region)

actual fun Locale.getDisplayScript(locale: Locale): String? =
    locale.toNSLocale().displayNameForKey(NSLocaleScriptCode, script)
