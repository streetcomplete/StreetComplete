package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.Month

/** Month name in given [locale] and [style]. If [locale] is null, the system default locale is
 *  used. */
expect fun Month.getDisplayName(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Full,
    locale: Locale? = null
): String
