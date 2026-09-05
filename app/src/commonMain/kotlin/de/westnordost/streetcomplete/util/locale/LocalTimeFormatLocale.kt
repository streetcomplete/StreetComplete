package de.westnordost.streetcomplete.util.locale

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale

/** Null means automatic formatting, including native user preferences such as 24-hour time. */
val LocalTimeFormatLocale = staticCompositionLocalOf<Locale?> { null }
