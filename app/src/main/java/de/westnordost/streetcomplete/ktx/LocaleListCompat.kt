package de.westnordost.streetcomplete.ktx

import androidx.core.os.LocaleListCompat
import java.util.Locale

fun LocaleListCompat.toList(): List<Locale> = (0 until size()).map { i -> this[i] }

fun LocaleListCompat.toTypedArray() = Array<Locale>(size()) { i -> this[i] }

/** Returns a copy of this locale list with the given locale added (or moved) to the front */
fun LocaleListCompat.addedToFront(locale: Locale): LocaleListCompat {
    val currentList = toList().filterNot { it == locale }.toTypedArray()
    return LocaleListCompat.create(locale, *currentList)
}
