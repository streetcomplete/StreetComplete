package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import kotlinx.datetime.DayOfWeek

/** Weekday name in given [locale] and [style]. If [locale] is null, the system default locale is
 *  used. E.g. "Monday" */
expect fun DayOfWeek.getDisplayName(
    style: DateTimeTextSymbolStyle = DateTimeTextSymbolStyle.Full,
    locale: Locale? = null
): String
