package de.westnordost.streetcomplete.util.ktx

/** Returns true if the map contains any of the specified [keys]. */
fun <K, V> Map<K, V>.containsAnyKey(vararg keys: K): Boolean = keys.any { this.keys.contains(it) }
