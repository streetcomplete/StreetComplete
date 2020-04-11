package de.westnordost.streetcomplete.ktx

import androidx.collection.LongSparseArray

val <E> LongSparseArray<E>.values: List<E> get() = (0 until size()).map { valueAt(it) }
val <E> LongSparseArray<E>.keys: List<Long> get() = (0 until size()).map { keyAt(it) }
