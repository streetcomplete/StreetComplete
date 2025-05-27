package de.westnordost.streetcomplete.util.ktx

import android.os.LocaleList
import java.util.Locale

// LocaleList.get(i) is only null if `i` is out of bounds but it is guaranteed here that it is not

fun LocaleList.toList(): List<Locale> = (0 until size()).map { i -> this[i]!! }

fun LocaleList.toTypedArray() = Array(size()) { i -> this[i]!! }

/** Returns a copy of this locale list with the given locale added (or moved) to the front */
fun LocaleList.addedToFront(locale: Locale): LocaleList {
    val currentList = toList().filterNot { it == locale }.toTypedArray()
    return LocaleList(locale, *currentList)
}
