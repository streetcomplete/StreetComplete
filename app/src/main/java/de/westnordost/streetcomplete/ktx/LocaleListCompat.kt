package de.westnordost.streetcomplete.ktx

import androidx.core.os.LocaleListCompat
import java.util.Locale

fun LocaleListCompat.toList(): List<Locale> = (0 until size()).map { i -> this[i] }

fun LocaleListCompat.toTypedArray() = Array<Locale>(size()) { i -> this[i] }
