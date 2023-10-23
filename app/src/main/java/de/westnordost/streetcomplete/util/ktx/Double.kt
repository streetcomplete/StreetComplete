package de.westnordost.streetcomplete.util.ktx

import java.util.Locale

fun Double.toShortString() = if (this % 1 == 0.0) toInt().toString() else toString()

fun Double.format(digits: Int) = "%.${digits}f".format(null, this)

fun Double.format(locale: Locale, digits: Int) = "%.${digits}f".format(locale, this)

fun Double.truncateTo5Decimals() = (this * 1e5).toInt().toDouble() / 1e5
