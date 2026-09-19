package de.westnordost.streetcomplete.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.LocaleList

/** Invalidates localized content while retaining the navigation stack and remembered form state. */
val LocalAppLocale = staticCompositionLocalOf { AppLocale(null, LocaleList.current) }

/** An explicit language and the system default may have the same tag but different formatting. */
data class AppLocale(val language: String?, val locales: LocaleList)
