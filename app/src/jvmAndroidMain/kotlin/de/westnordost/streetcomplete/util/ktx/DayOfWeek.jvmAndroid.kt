package de.westnordost.streetcomplete.util.ktx

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.locale.DateTimeTextSymbolStyle
import de.westnordost.streetcomplete.util.locale.toTextStyle
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber

actual fun DayOfWeek.getDisplayName(style: DateTimeTextSymbolStyle, locale: Locale?): String =
    java.time.DayOfWeek.of(isoDayNumber)
        .getDisplayName(style.toTextStyle(), (locale ?: Locale.current).platformLocale)
