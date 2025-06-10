package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException

private class NumberFormatterAndroid(locale: Locale?): NumberFormatter {

    private val format =
        if (locale != null) NumberFormat.getInstance(locale.platformLocale)
        else                NumberFormat.getInstance()

    private val symbols =
        if (locale != null) DecimalFormatSymbols.getInstance(locale.platformLocale)
        else                DecimalFormatSymbols.getInstance()

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
        // enforce strict parsing
        if (!text.all {
            it == symbols.decimalSeparator ||
            it == symbols.groupingSeparator && allowGrouping ||
            it.isDigit()
        }) return null

        format.isGroupingUsed = allowGrouping
        return format.parseOrNull(text)
    }

    override val decimalSeparator: Char
        get() = symbols.decimalSeparator

    override val groupingSeparator: Char
        get() = symbols.groupingSeparator
}

private fun NumberFormat.parseOrNull(text: String): Number? =
    try { parse(text) } catch (_: ParseException) { null }

actual fun NumberFormatter(locale: Locale?): NumberFormatter = NumberFormatterAndroid(locale)
