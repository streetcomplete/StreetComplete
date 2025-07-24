package de.westnordost.streetcomplete.util.ktx

import kotlin.math.pow

fun Double.toShortString(): String = if (this % 1 == 0.0) toInt().toString() else toString()

fun Double.format(decimals: Int): String = truncate(decimals).toString()

fun Double.truncateTo6Decimals(): Double = truncate(6)

fun Double.truncate(decimals: Int): Double {
    val factor = 10.0.pow(decimals)
    return (this * factor).toInt().toDouble() / factor
}
