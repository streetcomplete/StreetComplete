package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
import platform.Foundation.NSLocale
import platform.Foundation.currentLocale

/** The app's formatting locale, managed by AppLocaleUpdater independently of resource languages.
 *  Retaining the native system locale preserves user overrides such as the 12/24-hour clock. */
internal var appFormattingLocale: NSLocale = NSLocale.currentLocale

internal fun formatterLocale(locale: Locale?): NSLocale = locale?.toNSLocale() ?: appFormattingLocale
