package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle
import platform.Foundation.NSNumber

actual class NumberFormatter actual constructor(
    locale: Locale?,
    minIntegerDigits: Int,
    maxIntegerDigits: Int,
    minFractionDigits: Int,
    maxFractionDigits: Int,
    useGrouping: Boolean,
) {
    private val format = NSNumberFormatter().also {
        it.numberStyle = NSNumberFormatterDecimalStyle
        it.locale = (locale ?: Locale.current).platformLocale
        it.usesGroupingSeparator = useGrouping
        it.minimumIntegerDigits = minIntegerDigits.toULong()
        it.maximumIntegerDigits = maxIntegerDigits.toULong()
        it.minimumFractionDigits = minFractionDigits.toULong()
        it.maximumFractionDigits = maxFractionDigits.toULong()
        it.lenient = false
    }

    actual fun format(value: Number): String {
        return format.stringFromNumber(value as NSNumber)!!
    }

    actual fun parse(text: String): Number? {
        return format.numberFromString(text) as? Number
    }

    actual val decimalSeparator: Char
        get() = format.decimalSeparator.single()

    actual val groupingSeparator: Char
        get() = format.groupingSeparator.single()
}
