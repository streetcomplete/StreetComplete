package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import de.westnordost.streetcomplete.util.locale.toTextStyle
import kotlinx.datetime.Month
import kotlinx.datetime.number
import java.time.format.TextStyle

actual fun Month.getDisplayName(style: DateTimeTextSymbolStyle, locale: Locale?): String =
    getDisplayName(style.toTextStyle(), locale)

private fun Month.getDisplayName(textStyle: TextStyle, locale: Locale?): String =
    java.time.Month.of(number).getDisplayName(textStyle, (locale ?: Locale.current).platformLocale)
