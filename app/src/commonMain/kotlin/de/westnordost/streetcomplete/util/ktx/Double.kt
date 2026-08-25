package de.westnordost.streetcomplete.util.ktx

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.toShortString(): String = if (this % 1 == 0.0) toInt().toString() else toString()

fun Double.format(decimals: Int): String = truncate(decimals).toString()

fun Double.truncateTo6Decimals(): Double = truncate(6)

fun Double.truncate(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).toInt().toDouble() / factor
}

/** Formats this double to exactly the given number of [decimals].
 * E.g. both `12.305` and `12.3` become `"12.30"` when [decimals] is 2. */
fun Double.formatPadded(decimals: Int, omitZeroFraction: Boolean = false): String {
    val int = toInt()
    val fraction = ((this - toInt()) * 10.0.pow(decimals)).roundToInt()
    if (omitZeroFraction && fraction == 0) return int.toString()
    val fractionPadded = fraction.toString().padStart(decimals, '0')
    return "$int.$fractionPadded"
}
