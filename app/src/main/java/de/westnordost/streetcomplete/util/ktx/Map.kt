package de.westnordost.streetcomplete.util.ktx

fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }

/** Returns true if the map contains any of the specified [keys]. */
fun <K, V> Map<K, V>.containsAnyKey(vararg keys: K): Boolean = keys.any { this.keys.contains(it) }

// saves memory by interning the strings
// could save (noticeably!) more by using ArrayMap, but this slows down quest creation by ca. 15%
fun Map<String, String>.toInternedMap() = if (isEmpty()) emptyMap()
    else HashMap<String, String>(size, 0.9f).also {
        forEach { (k, v) -> it[k.intern()] = v.intern() }
    }
