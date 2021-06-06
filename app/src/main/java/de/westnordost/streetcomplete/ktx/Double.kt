package de.westnordost.streetcomplete.ktx

import java.util.*

fun Double.toShortString() = if (this % 1 == 0.0) toInt().toString() else toString()

fun Double.format(digits: Int) = "%.${digits}f".format(null, this)

fun Double.format(locale: Locale, digits: Int) = "%.${digits}f".format(locale, this)
