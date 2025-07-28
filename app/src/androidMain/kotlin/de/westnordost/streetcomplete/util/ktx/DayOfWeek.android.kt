package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import java.time.format.TextStyle

actual fun DayOfWeek.getDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.FULL_STANDALONE, locale)

actual fun DayOfWeek.getShortDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.SHORT_STANDALONE, locale)

actual fun DayOfWeek.getNarrowDisplayName(locale: Locale?): String =
    getDisplayName(TextStyle.NARROW_STANDALONE, locale)

private fun DayOfWeek.getDisplayName(textStyle: TextStyle, locale: Locale?): String =
    java.time.DayOfWeek.of(isoDayNumber).getDisplayName(textStyle, (locale ?: Locale.current).platformLocale)
