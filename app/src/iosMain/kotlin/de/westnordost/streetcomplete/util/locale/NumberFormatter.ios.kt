package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumber

actual class NumberFormatter actual constructor(locale: Locale?) {

    private val format = NSNumberFormatter().also {
        it.numberStyle = NSNumberFormatterDecimalStyle
        it.locale = (locale ?: Locale.current).platformLocale
        it.lenient = false
    }

    actual fun format(
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

    actual fun parse(text: String, allowGrouping: Boolean): Number? {
        format.usesGroupingSeparator = allowGrouping
        return format.numberFromString(text) as? Number
    }

    actual val decimalSeparator: Char
        get() = format.decimalSeparator.single()

    actual val groupingSeparator: Char
        get() = format.groupingSeparator.single()
}
