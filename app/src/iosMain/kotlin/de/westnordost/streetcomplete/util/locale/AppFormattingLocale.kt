package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import platform.Foundation.NSLocale

/** The app language, if one is selected. iOS has no process-wide default locale to set instead. */
internal var appFormattingLocale: Locale? = null

/** The locale a formatter should use for [locale], or null to keep the native defaults, which
 *  include the user's region formatting and 12/24-hour clock choice. */
internal fun formatterLocale(locale: Locale?): NSLocale? = (locale ?: appFormattingLocale)?.toNSLocale()
