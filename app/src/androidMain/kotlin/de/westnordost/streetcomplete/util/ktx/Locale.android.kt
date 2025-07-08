package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale

actual fun Locale.getName(locale: Locale): String? =
    platformLocale.getDisplayName(locale.platformLocale).takeIf { it != toLanguageTag() }

actual fun Locale.getLanguageName(locale: Locale): String? =
    platformLocale.getDisplayLanguage(locale.platformLocale).takeIf { it != language }

actual fun Locale.getRegionName(locale: Locale): String? =
    platformLocale.getDisplayCountry(locale.platformLocale).takeIf { it != region }

actual fun Locale.getScriptName(locale: Locale): String? =
    platformLocale.getDisplayScript(locale.platformLocale).takeIf { it != script }

