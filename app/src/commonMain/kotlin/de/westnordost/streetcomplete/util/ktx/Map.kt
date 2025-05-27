package de.westnordost.streetcomplete.util.ktx

/** Return whether this map contains the given [other] completely */
fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }
