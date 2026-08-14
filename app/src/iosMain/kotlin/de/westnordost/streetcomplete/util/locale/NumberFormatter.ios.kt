package de.westnordost.streetcomplete.util.locale

import androidx.compose.ui.text.intl.Locale
import de.westnordost.streetcomplete.util.ktx.toNSLocale
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
        it.locale = (locale ?: Locale.current).toNSLocale()
        it.usesGroupingSeparator = useGrouping
        it.minimumIntegerDigits = minIntegerDigits.toULong()
        it.maximumIntegerDigits = maxIntegerDigits.toULong()
        it.minimumFractionDigits = minFractionDigits.toULong()
        it.maximumFractionDigits = maxFractionDigits.toULong()
        it.lenient = false
    }

    actual fun format(value: Number): String {
        return format.stringFromNumber(value.toNSNumber())!!
    }

    actual fun parse(text: String): Number? {
        return format.numberFromString(text)?.toNumber()
    }

    actual val decimalSeparator: Char
        get() = format.decimalSeparator.single()

    actual val groupingSeparator: Char
        get() = format.groupingSeparator.single()
}

// Kotlin's number types are not NSNumbers in Kotlin/Native, they must be converted explicitly

private fun Number.toNSNumber(): NSNumber = when (this) {
    is Byte -> NSNumber(char = this)
    is Short -> NSNumber(short = this)
    is Int -> NSNumber(int = this)
    is Long -> NSNumber(longLong = this)
    is Float -> NSNumber(float = this)
    is Double -> NSNumber(double = this)
    else -> NSNumber(double = toDouble())
}

/** Like NumberFormat.parse on the JVM, returns a Long if the parsed number is integral */
private fun NSNumber.toNumber(): Number {
    val double = doubleValue
    val long = longLongValue
    return if (long.toDouble() == double) long else double
}
