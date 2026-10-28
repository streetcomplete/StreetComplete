package de.westnordost.streetcomplete.util.ktx

/** Return whether this map contains the given [other] completely */
fun <X, Y> Map<X, Y>.containsAll(other: Map<X, Y>) = other.all { this[it.key] == it.value }

/** Gets a value from a map or inserts a default if the key isn't in the map yet.
 *
 * Replacement for Kotlin's [MutableMap.getOrPutIfMissing] which requires
 * opting into unstable standard library functions.
 */
fun <X, Y> MutableMap<X, Y>.getOrPutIfMissing(key: X, defaultValue: () -> Y) : Y {
    if(!containsKey(key)) {
        val value = defaultValue()
        put(key, value)
        return value
    } else {
        return getValue(key)
    }
}

/** Gets a value from a map or inserts the default value of the map returned
 *  from `getValue` if the key isn't in the map yet.
 */
fun <X, Y> MutableMap<X, Y>.getOrPutDefaultIfMissing(key: X) : Y {
    val value = getValue(key)
    put(key, value)
    return value
}
