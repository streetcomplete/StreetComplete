package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.NSLocaleScriptCode

actual fun Locale.getDisplayName(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleIdentifier, locale.toLanguageTag())

actual fun Locale.getDisplayLanguage(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleLanguageCode, locale.language)

actual fun Locale.getDisplayRegion(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleCountryCode, locale.region)

actual fun Locale.getDisplayScript(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleScriptCode, locale.script)
