package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumber

private class NumberFormatterIos(locale: Locale?): NumberFormatter {

    private val format = NSNumberFormatter().also {
        it.numberStyle = NSNumberFormatterDecimalStyle
        it.locale = (locale ?: Locale.current).platformLocale
        it.lenient = false
    }

    override fun format(
        value: Number,
        minFractionDigits: Int,
        maxFractionDigits: Int,
        useGrouping: Boolean
    ): String {
        format.usesGroupingSeparator = useGrouping
        format.minimumFractionDigits = minFractionDigits.toULong()
        format.maximumFractionDigits = maxFractionDigits.toULong()
        return format.stringFromNumber(value as NSNumber)!!
    }

    override fun parse(text: String, allowGrouping: Boolean): Number? {
        format.usesGroupingSeparator = allowGrouping
        return format.numberFromString(text) as? Number
    }

    override val decimalSeparator: Char
        get() = format.decimalSeparator.single()

    override val groupingSeparator: Char
        get() = format.groupingSeparator.single()
}

actual fun NumberFormatter(locale: Locale?): NumberFormatter = NumberFormatterIos(locale)
