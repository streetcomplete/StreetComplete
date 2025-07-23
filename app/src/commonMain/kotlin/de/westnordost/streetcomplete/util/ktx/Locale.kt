package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale

/** Human-readable locale name in current locale. Returns `null` if no name is available. */
val Locale.name: String? get() = getName(Locale.current)

/** Human-readable language name in current locale. Returns `null` if no name is available. */
val Locale.languageName: String? get() = getLanguageName(Locale.current)

/** Human-readable country name in current locale. Returns `null` if no name is available. */
val Locale.countryName: String? get() = getRegionName(Locale.current)

/** Human-readable script name in current locale. Returns `null` if no name is available. */
val Locale.scriptName: String? get() = getScriptName(Locale.current)

/** Human-readable locale name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getName(locale: Locale): String?

/** Human-readable language name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getLanguageName(locale: Locale): String?

/** Human-readable country name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getRegionName(locale: Locale): String?

/** Human-readable script name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getScriptName(locale: Locale): String?
