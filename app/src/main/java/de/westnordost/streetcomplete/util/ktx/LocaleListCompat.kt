package de.westnordost.streetcomplete.util.ktx

import androidx.core.os.LocaleListCompat
import java.util.Locale

// LocaleListCompat.get(i) is only null if `i` is out of bounds but it is guaranteed here that it is not

fun LocaleListCompat.toList(): List<Locale> = (0 until size()).map { i -> this[i]!! }

fun LocaleListCompat.toTypedArray() = Array(size()) { i -> this[i]!! }

/** Returns a copy of this locale list with the given locale added (or moved) to the front */
fun LocaleListCompat.addedToFront(locale: Locale): LocaleListCompat {
    val currentList = toList().filterNot { it == locale }.toTypedArray()
    return LocaleListCompat.create(locale, *currentList)
}
