package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSLocaleIdentifier
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleLanguageCode
import platform.Foundation.NSLocaleScriptCode

actual fun Locale.getName(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleIdentifier, locale.toLanguageTag())

actual fun Locale.getLanguageName(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleLanguageCode, locale.language)

actual fun Locale.getRegionName(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleCountryCode, locale.region)

actual fun Locale.getScriptName(locale: Locale): String? =
    locale.platformLocale.displayNameForKey(NSLocaleScriptCode, locale.script)
