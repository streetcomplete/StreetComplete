package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale

/** Human-readable locale name in current locale. Returns `null` if no name is available. */
val Locale.displayName: String? get() = getDisplayName(Locale.current)

/** Human-readable language name in current locale. Returns `null` if no name is available. */
val Locale.displayLanguage: String? get() = getDisplayLanguage(Locale.current)

/** Human-readable region name in current locale. Returns `null` if no name is available. */
val Locale.displayRegion: String? get() = getDisplayRegion(Locale.current)

/** Human-readable script name in current locale. Returns `null` if no name is available. */
val Locale.displayScript: String? get() = getDisplayScript(Locale.current)

/** Human-readable locale name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getDisplayName(locale: Locale): String?

/** Human-readable language name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getDisplayLanguage(locale: Locale): String?

/** Human-readable region name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getDisplayRegion(locale: Locale): String?

/** Human-readable script name in given [locale]. Returns `null` if no name is available. */
expect fun Locale.getDisplayScript(locale: Locale): String?
