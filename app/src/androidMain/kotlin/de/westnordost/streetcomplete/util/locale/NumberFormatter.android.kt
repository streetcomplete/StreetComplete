package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import java.text.DecimalFormatSymbols
import java.text.NumberFormat
import java.text.ParseException

actual class NumberFormatter actual constructor(
    locale: Locale?,
    minIntegerDigits: Int,
    maxIntegerDigits: Int,
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean,
) {
    private val format: NumberFormat =
        if (locale != null) NumberFormat.getInstance(locale.platformLocale)
        else                NumberFormat.getInstance()

    private val symbols =
        if (locale != null) DecimalFormatSymbols.getInstance(locale.platformLocale)
        else                DecimalFormatSymbols.getInstance()

    init {
        format.isGroupingUsed = useGrouping
        format.minimumIntegerDigits = minIntegerDigits
        format.maximumIntegerDigits = maxIntegerDigits
        format.minimumFractionDigits = minFractionDigits
        format.maximumFractionDigits = maxFractionDigits
    }

    actual fun format(value: Number): String {
        return format.format(value)
    }

    actual fun parse(text: String): Number? {
        // enforce strict parsing (Java parser is really lenient...)
        if (!text.all {
            it == symbols.decimalSeparator ||
            it == symbols.groupingSeparator && format.isGroupingUsed ||
            it.isDigit()
        }) return null

        return format.parseOrNull(text)
    }

    actual val decimalSeparator: Char
        get() = symbols.decimalSeparator

    actual val groupingSeparator: Char
        get() = symbols.groupingSeparator
}

private fun NumberFormat.parseOrNull(text: String): Number? =
    try { parse(text) } catch (_: ParseException) { null }
