package de.westnordost.streetcomplete.util.ktx

/** Returns true if the map contains any of the specified [keys]. */
fun <K, V> Map<K, V>.containsAnyKey(vararg keys: K): Boolean = keys.any { this.keys.contains(it) }

fun Map<String, String>.toInternedHashMap() = HashMap<String, String>(size, 0.9f).apply {
    forEach { (k, v) -> put(k.intern(), v.intern()) }
}
