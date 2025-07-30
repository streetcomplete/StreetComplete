package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale

actual fun Locale.getDisplayName(locale: Locale): String? =
    platformLocale.getDisplayName(locale.platformLocale).takeIf { it != toLanguageTag() }

actual fun Locale.getDisplayLanguage(locale: Locale): String? =
    platformLocale.getDisplayLanguage(locale.platformLocale).takeIf { it != language }

actual fun Locale.getDisplayRegion(locale: Locale): String? =
    platformLocale.getDisplayCountry(locale.platformLocale).takeIf { it != region }

actual fun Locale.getDisplayScript(locale: Locale): String? =
    platformLocale.getDisplayScript(locale.platformLocale).takeIf { it != script }

