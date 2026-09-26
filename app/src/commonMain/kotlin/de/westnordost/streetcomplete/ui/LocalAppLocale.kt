package de.westnordost.streetcomplete.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList

/** Invalidates localized content while retaining the navigation stack and remembered form state. */
val LocalAppLocale = staticCompositionLocalOf { AppLocale(null, LocaleList.current) }

/** The selected locale (null for system default) and the effective resource fallback locales. */
data class AppLocale(val locale: Locale?, val locales: LocaleList)
