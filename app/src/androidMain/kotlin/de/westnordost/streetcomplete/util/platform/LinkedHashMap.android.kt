package de.westnordost.streetcomplete.util.platform

actual fun <K, V> linkedHashMapWithAccessOrder(
    initialCapacity: Int,
    loadFactor: Float,
    accessOrder: Boolean,
): LinkedHashMap<K, V> = java.util.LinkedHashMap(initialCapacity, loadFactor, accessOrder)
