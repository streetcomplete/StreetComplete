package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import java.text.NumberFormat
import java.text.ParseException

private class NumberFormatterAndroid(locale: Locale?): NumberFormatter {

    private val format =
        if (locale != null) NumberFormat.getInstance(locale.platformLocale)
        else                NumberFormat.getInstance()

    override fun format(
        value: Number,
        minFractionDigits: Int,
        maxFractionDigits: Int,
        useGrouping: Boolean
    ): String {
        format.isGroupingUsed = useGrouping
        format.minimumFractionDigits = minFractionDigits
        format.maximumFractionDigits = maxFractionDigits
        return format.format(value)
    }

    override fun parse(text: String, allowGrouping: Boolean): Number? {
        format.isGroupingUsed = allowGrouping
        return format.parseOrNull(text)
    }
}

private fun NumberFormat.parseOrNull(text: String): Number? =
    try { parse(text) } catch (_: ParseException) { null }

actual fun NumberFormatter(locale: Locale?): NumberFormatter = NumberFormatterAndroid(locale)
