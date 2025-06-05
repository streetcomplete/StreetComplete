package de.westnordost.streetcomplete.util.platform

/**
 * Creates a new empty [linkedHashMapWithAccessOrder].
 *
 * @param initialCapacity the initial capacity
 * @param loadFactor the load factor
 * @param accessOrder the ordering mode - true for access-order, false for insertion-order
 *                    Note: This is only available on JVM, i.e. on other platforms, this parameter
 *                    is ignored.
 *
 * @throws IllegalArgumentException if [initialCapacity] is negative or [loadFactor] is non-positive
 */
expect fun <K, V> linkedHashMapWithAccessOrder(
    initialCapacity: Int,
    loadFactor: Float,
    accessOrder: Boolean
): LinkedHashMap<K, V>
