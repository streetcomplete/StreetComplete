package de.westnordost.streetcomplete.util.ktx

import java.util.Locale

fun Double.format(locale: Locale, digits: Int) = "%.${digits}f".format(locale, this)
