package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.Month
import kotlinx.datetime.number
import java.time.format.TextStyle

actual fun Month.getDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.FULL_STANDALONE, locale)

actual fun Month.getShortDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.SHORT_STANDALONE, locale)

actual fun Month.getNarrowDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.NARROW_STANDALONE, locale)

private fun Month.getDisplayName(textStyle: TextStyle, locale: Locale?): String =
    java.time.Month.of(number).getDisplayName(textStyle, (locale ?: Locale.current).platformLocale)
